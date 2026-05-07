package dev.felnull.itts.core;

import com.google.common.collect.ImmutableList;
import dev.felnull.itts.core.audio.VoiceAudioManager;
import dev.felnull.itts.core.cache.CacheManager;
import dev.felnull.itts.core.config.ConfigManager;
import dev.felnull.itts.core.config.MetricsConfig;
import dev.felnull.itts.core.dict.DictionaryManager;
import dev.felnull.itts.core.discord.Bot;
import dev.felnull.itts.core.metrics.MetricsRegistry;
import dev.felnull.itts.core.metrics.PrometheusHttpExposer;
import dev.felnull.itts.core.savedata.SaveDataManager;
import dev.felnull.itts.core.savedata.repository.DataRepository;
import dev.felnull.itts.core.tts.TTSCountRecorder;
import dev.felnull.itts.core.tts.TTSManager;
import dev.felnull.itts.core.voice.VoiceManager;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * I-TTSの実行関係
 *
 * @author MORIMORI0317
 */
public class ITTSRuntime {
    /**
     * I-TTSのインスタンス
     */
    private static ITTSRuntime instance;

    /**
     * ロガー
     */
    private final Logger logger;

    /**
     * 非同期処理用エクスキューター
     */
    private final ExecutorService asyncWorkerExecutor = Executors.newCachedThreadPool(new BasicThreadFactory.Builder()
            .namingPattern("async-worker-%d")
            .daemon(true)
            .build());

    /**
     * HTTP接続の制御を行うためのエクスキューター
     */
    private final ExecutorService httpWorkerExecutor = Executors.newCachedThreadPool(new BasicThreadFactory.Builder()
            .namingPattern("http-worker-%d")
            .daemon(true)
            .build());

    /**
     * 重い処理を行うエクスキューター
     */
    private final ExecutorService heavyWorkerExecutor = Executors.newFixedThreadPool(Math.max(Runtime.getRuntime().availableProcessors(), 1),
            new BasicThreadFactory.Builder().namingPattern("heavy-worker-thread-%d").daemon(true).build());

    /**
     * タイマー
     */
    private final ImmortalityTimer immortalityTimer = new ImmortalityTimer(new Timer("immortality-timer", true));

    /**
     * ディレクトリロック
     */
    private final DirectoryLock directoryLock = new DirectoryLock();

    /**
     * バージョン
     */
    private final String version;

    /**
     * 開発環境かどうか
     */
    private final boolean developmentEnvironment;

    /**
     * コンフィグマネージャー
     */
    private final ConfigManager configManager;

    /**
     * TTSマネージャー
     */
    private final TTSManager ttsManager = new TTSManager();

    /**
     * ボイスマネージャー
     */
    private final VoiceManager voiceManager = new VoiceManager();

    /**
     * オーディオマネージャー
     */
    private final VoiceAudioManager voiceAudioManager = new VoiceAudioManager();

    /**
     * 辞書マネージャー
     */
    private final DictionaryManager dictionaryManager = new DictionaryManager();

    /**
     * ネットワークマネージャー
     */
    private final ITTSNetworkManager networkManager = new ITTSNetworkManager();

    /**
     * キャッシュマネージャー
     */
    private final CacheManager cacheManager;

    /**
     * DiscordのBOT関係
     */
    private final Bot bot;

    /**
     * 全てのマネージャー
     */
    private final List<ITTSBaseManager> managers;

    /**
     * 起動した時の時刻
     */
    private long startupTime;

    /**
     * Prometheusメトリクスのレジストリ
     */
    private MetricsRegistry metricsRegistry;

    /**
     * Prometheusメトリクス公開HTTPサーバー
     */
    private PrometheusHttpExposer prometheusHttpExposer;

    /**
     * 読み上げ文字数のレコーダー
     */
    private TTSCountRecorder ttsCountRecorder;

    private ITTSRuntime(ITTSRuntimeContext runtimeContext) {
        if (instance != null) {
            throw new IllegalStateException("ITTSRuntime must be a singleton instance");
        }

        instance = this;

        directoryLock.lock();

        this.logger = runtimeContext.getLogContext().getLogger();

        String v = ITTSRuntime.class.getPackage().getImplementationVersion();
        this.developmentEnvironment = v == null;
        this.version = Objects.requireNonNullElse(v, "None");

        this.bot = new Bot();
        this.configManager = new ConfigManager(runtimeContext.getConfigContext());
        this.cacheManager = new CacheManager(runtimeContext.getGlobalCacheAccessFactory());

        this.managers = ImmutableList.of(configManager, voiceManager);
    }

    /**
     * Runtimeインスタンスを取得
     *
     * @return Runtimeインスタンス
     */
    public static ITTSRuntime getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Instance does not exist");
        }

        return instance;
    }

    /**
     * ランタイムを作成
     *
     * @param runtimeContext コンテキスト
     * @return ランタイム
     */
    public static ITTSRuntime newRuntime(@NotNull ITTSRuntimeContext runtimeContext) {
        return new ITTSRuntime(runtimeContext);
    }

    /**
     * 実行
     */
    public void execute() {
        startupTime = System.currentTimeMillis();

        logger.info("The Ikisugi-TTS ({})", getVersionText());

        logger.info("--System info--");
        logger.info("Java runtime: {}", System.getProperty("java.runtime.name"));
        logger.info("Java version: {}", System.getProperty("java.version"));
        logger.info("Java vm name: {}", System.getProperty("java.vm.name"));
        logger.info("Java vm version: {}", System.getProperty("java.vm.version"));
        logger.info("OS: {}", System.getProperty("os.name"));
        logger.info("Arch: {}", System.getProperty("os.arch"));
        logger.info("Available Processors: {}", Runtime.getRuntime().availableProcessors());
        logger.info("---------------");

        logger.info("Start setup");

        managers.stream()
                .map(ITTSBaseManager::init)
                .forEach(CompletableFuture::join);

        SaveDataManager.getInstance().init();

        logger.info("Setup complete");

        initMetrics();

        bot.start();
    }

    private void initMetrics() {
        MetricsConfig metricsConfig = configManager.getConfig().getMetricsConfig();
        this.metricsRegistry = metricsConfig.isEnabled() ? new MetricsRegistry() : null;
        this.ttsCountRecorder = new TTSCountRecorder(metricsRegistry);

        if (metricsRegistry == null) {
            logger.info("Prometheus metrics is disabled");
            return;
        }

        try {
            this.prometheusHttpExposer = new PrometheusHttpExposer(metricsRegistry);
            this.prometheusHttpExposer.start(metricsConfig.getBindAddress(), metricsConfig.getPort());
            logger.info("Prometheus metrics endpoint started on {}:{}/metrics", metricsConfig.getBindAddress(), metricsConfig.getPort());
            warmupMetricsCounters();
        } catch (Exception e) {
            logger.warn("Failed to start Prometheus HTTP exposer", e);
            this.prometheusHttpExposer = null;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (this.prometheusHttpExposer != null) {
                this.prometheusHttpExposer.stop();
            }
        }, "prometheus-exposer-shutdown"));
    }

    private void warmupMetricsCounters() {
        if (metricsRegistry == null) {
            return;
        }

        DataRepository repo = SaveDataManager.getInstance().getRepository();
        if (repo == null) {
            return;
        }

        try {
            long botId = bot.getBotId();
            long charTotal = repo.sumGlobalAllCharCount(botId);
            long messageTotal = repo.sumGlobalAllMessageCount(botId);
            if (charTotal > 0) {
                metricsRegistry.getOrCreateCharCounter(botId, null).increment(charTotal);
            }
            if (messageTotal > 0) {
                metricsRegistry.getOrCreateMessageCounter(botId, null).increment(messageTotal);
            }
        } catch (Throwable t) {
            logger.warn("Failed to warmup metrics counters", t);
        }
    }

    public long getStartupTime() {
        return startupTime;
    }

    public Executor getAsyncWorkerExecutor() {
        return asyncWorkerExecutor;
    }

    public ExecutorService getHeavyWorkerExecutor() {
        return heavyWorkerExecutor;
    }

    public ExecutorService getHttpWorkerExecutor() {
        return httpWorkerExecutor;
    }

    public ImmortalityTimer getImmortalityTimer() {
        return immortalityTimer;
    }

    public boolean isDevelopmentEnvironment() {
        return developmentEnvironment;
    }

    public String getVersion() {
        return version;
    }

    public String getVersionText() {
        return isDevelopmentEnvironment() ? "開発環境" : "v" + getVersion();
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public Logger getLogger() {
        return logger;
    }

    public TTSManager getTTSManager() {
        return ttsManager;
    }

    public VoiceAudioManager getVoiceAudioManager() {
        return voiceAudioManager;
    }

    public VoiceManager getVoiceManager() {
        return voiceManager;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public DictionaryManager getDictionaryManager() {
        return dictionaryManager;
    }


    public ITTSNetworkManager getNetworkManager() {
        return networkManager;
    }

    public Bot getBot() {
        return bot;
    }

    /**
     * 読み上げ文字数のレコーダーを取得
     *
     * @return レコーダー
     */
    public TTSCountRecorder getTTSCountRecorder() {
        return ttsCountRecorder;
    }

    /**
     * Prometheusメトリクスのレジストリを取得
     *
     * @return レジストリ nullの場合はメトリクス無効
     */
    public MetricsRegistry getMetricsRegistry() {
        return metricsRegistry;
    }
}
