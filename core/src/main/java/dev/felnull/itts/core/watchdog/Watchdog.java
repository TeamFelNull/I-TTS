package dev.felnull.itts.core.watchdog;

import net.dv8tion.jda.api.JDA;
import org.apache.logging.log4j.Logger;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * 動作不能になったプロセスを検知して自ら終了させる監視処理
 *
 * <p>スレッドを作れない状況でも動けるよう、専用のスレッドを起動時に1本だけ確保して使い続ける。
 * このスレッド内では待機と軽い読み取りだけを行い、新しいスレッドは作らない。</p>
 */
public final class Watchdog {

    /**
     * 監視スレッドの名前
     */
    public static final String THREAD_NAME = "watchdog";

    /**
     * 監視の間隔
     */
    private static final Duration CHECK_INTERVAL = Duration.ofSeconds(30);

    /**
     * 状態をログに出す間隔
     */
    private static final Duration REPORT_INTERVAL = Duration.ofMinutes(10);

    /**
     * 常にログへ出すスレッドの分類。本数が0でも出し、増減を追えるようにする
     */
    private static final List<String> REPORT_PINNED = List.of("jda-audio-read", "jda-audio-write", "jda-audio-send",
            "lava-playback", "async", "http", "unnamed");

    /**
     * 上記以外でログに出すスレッドの分類数の上限
     */
    private static final int REPORT_MAX_OTHERS = 5;

    /**
     * 終了時の終了コード
     */
    private static final int EXIT_CODE = 70;

    /**
     * ロガー
     */
    private final Logger logger;

    /**
     * 判定
     */
    private final WatchdogPolicy policy;

    /**
     * JDAの供給元。起動前はnullを返す
     */
    private final Supplier<JDA> jdaSupplier;

    /**
     * 接続中のVC数の供給元
     */
    private final IntSupplier voiceConnectionCount;

    /**
     * 時刻の供給元(ミリ秒)
     */
    private final LongSupplier clock;

    /**
     * 強制終了の処理
     */
    private final IntConsumer halter;

    /**
     * スレッド名の取得元
     */
    private final Supplier<List<String>> threadNames;

    /**
     * 上限と現在値の取得元
     */
    private final Supplier<ProcessLimits.Snapshot> limitsSupplier;

    /**
     * スリープ処理
     */
    private final Sleeper sleeper;

    /**
     * 次に状態をログへ出す時刻(ミリ秒)
     */
    private long nextReportAt;

    /**
     * 開始済みかどうか
     */
    private boolean started;

    /**
     * 停止が要求されたかどうか
     */
    private volatile boolean stopRequested;

    /**
     * 監視スレッド
     */
    private Thread thread;

    /**
     * コンストラクタ
     *
     * @param logger               ロガー
     * @param policy               判定
     * @param jdaSupplier          JDAの供給元
     * @param voiceConnectionCount 接続中のVC数の供給元
     */
    public Watchdog(Logger logger, WatchdogPolicy policy, Supplier<JDA> jdaSupplier, IntSupplier voiceConnectionCount) {
        this(logger, policy, jdaSupplier, voiceConnectionCount, System::currentTimeMillis,
                code -> Runtime.getRuntime().halt(code), Watchdog::currentThreadNames, ProcessLimits::read,
                duration -> Thread.sleep(duration.toMillis()));
    }

    /**
     * テスト用のコンストラクタ
     *
     * @param logger               ロガー
     * @param policy               判定
     * @param jdaSupplier          JDAの供給元
     * @param voiceConnectionCount 接続中のVC数の供給元
     * @param clock                時刻の供給元
     * @param halter               強制終了の処理
     * @param threadNames          スレッド名の取得元
     * @param limitsSupplier       上限と現在値の取得元
     * @param sleeper              スリープ処理
     */
    Watchdog(Logger logger, WatchdogPolicy policy, Supplier<JDA> jdaSupplier, IntSupplier voiceConnectionCount,
             LongSupplier clock, IntConsumer halter, Supplier<List<String>> threadNames,
             Supplier<ProcessLimits.Snapshot> limitsSupplier, Sleeper sleeper) {
        this.logger = logger;
        this.policy = policy;
        this.jdaSupplier = jdaSupplier;
        this.voiceConnectionCount = voiceConnectionCount;
        this.clock = clock;
        this.halter = halter;
        this.threadNames = threadNames;
        this.limitsSupplier = limitsSupplier;
        this.sleeper = sleeper;
    }

    /**
     * 監視を開始する
     * 起動直後の早い段階で呼び、専用スレッドを確保しておく
     */
    public synchronized void start() {
        if (this.started) {
            return;
        }
        this.started = true;

        logStartup();
        this.nextReportAt = this.clock.getAsLong() + REPORT_INTERVAL.toMillis();

        this.thread = new Thread(this::run, THREAD_NAME);
        this.thread.setDaemon(true);
        this.thread.start();
    }

    /**
     * 監視を停止する
     */
    public synchronized void stop() {
        this.stopRequested = true;
        if (this.thread != null) {
            this.thread.interrupt();
        }
    }

    private void run() {
        while (!this.stopRequested) {
            try {
                tick();
            } catch (Throwable t) {
                // 監視自体が静かに止まらないよう、何があっても続ける
                safeLog("Watchdog check failed: " + describe(t));
            }

            try {
                this.sleeper.sleep(CHECK_INTERVAL);
            } catch (InterruptedException e) {
                // 停止要求以外で割り込まれても、監視は続ける
                if (this.stopRequested) {
                    return;
                }
            }
        }
    }

    /**
     * 1回分の監視を行う
     */
    void tick() {
        long now = this.clock.getAsLong();

        WatchdogPolicy.Verdict connection = this.policy.evaluateConnection(now, currentConnectionState());
        if (connection.fatal()) {
            die(connection.reason());
            return;
        }

        ProcessLimits.Snapshot limits = this.limitsSupplier.get();
        OptionalLong current = limits.current().isPresent() ? limits.current() : OptionalLong.of(this.threadNames.get().size());
        WatchdogPolicy.Verdict threads = this.policy.evaluateThreads(current, limits.limit());
        if (threads.fatal()) {
            die(threads.reason());
            return;
        }

        if (now >= this.nextReportAt) {
            this.nextReportAt = now + REPORT_INTERVAL.toMillis();
            logReport(limits, current);
        }
    }

    private WatchdogPolicy.ConnectionState currentConnectionState() {
        JDA jda = this.jdaSupplier.get();
        if (jda == null) {
            return WatchdogPolicy.ConnectionState.HEALTHY;
        }

        return classify(jda.getStatus());
    }

    /**
     * JDAの状態を監視上の分類に変換する
     * 起動処理中と接続済み、終了処理中は対象外として扱う
     *
     * @param status JDAの状態
     * @return 分類
     */
    static WatchdogPolicy.ConnectionState classify(JDA.Status status) {
        return switch (status) {
            case DISCONNECTED, RECONNECT_QUEUED, WAITING_TO_RECONNECT, ATTEMPTING_TO_RECONNECT -> WatchdogPolicy.ConnectionState.DISCONNECTED;
            default -> WatchdogPolicy.ConnectionState.HEALTHY;
        };
    }

    private void die(String reason) {
        // ログは同期で書かれる。その直後に待たずに強制終了する
        safeLog("Watchdog: terminating the process: " + reason);
        this.halter.accept(EXIT_CODE);
    }

    private void logStartup() {
        ProcessLimits.Snapshot limits = this.limitsSupplier.get();
        this.logger.info("Process limits: threads-limit={} current={} source={} jvm-threads={}",
                describeLimit(limits.limit(), limits.source()), describeValue(limits.current()), limits.source(), this.threadNames.get().size());
    }

    private void logReport(ProcessLimits.Snapshot limits, OptionalLong current) {
        List<String> names = this.threadNames.get();
        Map<String, Integer> counts = ThreadNameClassifier.count(names);
        JDA jda = this.jdaSupplier.get();

        // totalの出典: OSから読めた場合はOS側の値、読めない場合はJVM内のJavaスレッド数(ネイティブスレッドを含まない)
        String totalSource = limits.current().isPresent() ? limits.source() : "jvm";

        this.logger.info("Threads: total={}({}) limit={} jvm={} | {} | vc={} jda={}",
                describeValue(current), totalSource, describeLimit(limits.limit(), limits.source()), names.size(),
                ThreadNameClassifier.format(counts, REPORT_PINNED, REPORT_MAX_OTHERS),
                this.voiceConnectionCount.getAsInt(), jda == null ? "none" : jda.getStatus());
    }

    private static String describeValue(OptionalLong value) {
        return value.isPresent() ? Long.toString(value.getAsLong()) : "unavailable";
    }

    private static String describeLimit(OptionalLong limit, String source) {
        if (limit.isPresent()) {
            return Long.toString(limit.getAsLong());
        }
        return "unavailable".equals(source) ? "unavailable" : "unlimited-or-unknown";
    }

    private void safeLog(String message) {
        try {
            this.logger.error(message);
        } catch (Throwable ignored) {
            // ログに書けなくても終了処理は止めない
            System.err.println(message);
        }
    }

    private static String describe(Throwable t) {
        Throwable root = t;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        String msg = root.getMessage();
        return root.getClass().getSimpleName() + (msg == null ? "" : ": " + msg);
    }

    private static List<String> currentThreadNames() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        long[] ids = bean.getAllThreadIds();
        List<String> names = new ArrayList<>(ids.length);
        for (ThreadInfo info : bean.getThreadInfo(ids)) {
            if (info != null) {
                names.add(info.getThreadName());
            }
        }
        return names;
    }

    /**
     * スリープ処理
     */
    @FunctionalInterface
    interface Sleeper {
        /**
         * 指定した時間だけ待つ
         *
         * @param duration 待つ時間
         * @throws InterruptedException 待機中に割り込まれた場合
         */
        void sleep(Duration duration) throws InterruptedException;
    }
}
