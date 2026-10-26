package dev.felnull.itts.core.watchdog;

import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.OptionalLong;

/**
 * 異常状態が続いているかどうかを判定する
 * 時刻と状態だけを受け取る副作用のない処理として、単体テストで確かめられるようにしている
 */
public final class WatchdogPolicy {

    /**
     * 接続が切れたままの状態が続いた時に終了とみなす時間
     */
    private final Duration disconnectLimit;

    /**
     * スレッド数が上限のどれだけに達したら終了とみなすか(0より大きく1以下)
     */
    private final double threadUsageLimit;

    /**
     * 接続できていない状態が始まった時刻(ミリ秒)。接続できている間はnull
     */
    @Nullable
    private Long disconnectedSince;

    /**
     * コンストラクタ
     *
     * @param disconnectLimit  接続が切れたままの状態を許容する時間
     * @param threadUsageLimit スレッド数の使用率の上限
     */
    public WatchdogPolicy(Duration disconnectLimit, double threadUsageLimit) {
        if (disconnectLimit.isNegative() || disconnectLimit.isZero()) {
            throw new IllegalArgumentException("disconnectLimit must be positive");
        }
        if (threadUsageLimit <= 0 || threadUsageLimit > 1) {
            throw new IllegalArgumentException("threadUsageLimit must be in (0, 1]");
        }

        this.disconnectLimit = disconnectLimit;
        this.threadUsageLimit = threadUsageLimit;
    }

    /**
     * 接続状態を評価する
     * 起動処理中や、通常の接続状態は正常として扱い、時間のカウントを止める
     *
     * @param nowMillis 現在時刻(ミリ秒)
     * @param state     接続状態
     * @return 判定結果
     */
    public Verdict evaluateConnection(long nowMillis, ConnectionState state) {
        if (state != ConnectionState.DISCONNECTED) {
            this.disconnectedSince = null;
            return Verdict.OK;
        }

        if (this.disconnectedSince == null) {
            this.disconnectedSince = nowMillis;
        }

        long elapsed = nowMillis - this.disconnectedSince;
        if (elapsed >= this.disconnectLimit.toMillis()) {
            return new Verdict(true, "Discord connection has not recovered for " + Duration.ofMillis(elapsed).toSeconds() + "s");
        }

        return Verdict.OK;
    }

    /**
     * スレッド数を評価する
     * 上限が不明な場合は判定しない
     *
     * @param current 現在のスレッド数
     * @param limit   上限
     * @return 判定結果
     */
    public Verdict evaluateThreads(OptionalLong current, OptionalLong limit) {
        if (current.isEmpty() || limit.isEmpty() || limit.getAsLong() <= 0) {
            return Verdict.OK;
        }

        double usage = (double) current.getAsLong() / limit.getAsLong();
        if (usage >= this.threadUsageLimit) {
            return new Verdict(true, "Thread usage " + current.getAsLong() + "/" + limit.getAsLong()
                    + " reached " + Math.round(usage * 100) + "%");
        }

        return Verdict.OK;
    }

    /**
     * 異常の判定結果
     *
     * @param fatal  終了すべきかどうか
     * @param reason 理由。終了すべきでない場合は空文字
     */
    public record Verdict(boolean fatal, String reason) {
        /**
         * 正常
         */
        private static final Verdict OK = new Verdict(false, "");
    }

    /**
     * 接続状態の分類
     */
    public enum ConnectionState {
        /**
         * 接続できている、または起動処理中で判定の対象外
         */
        HEALTHY,

        /**
         * 接続が切れていて、まだ復旧していない
         */
        DISCONNECTED
    }
}
