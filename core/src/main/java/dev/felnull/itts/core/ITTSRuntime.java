package dev.felnull.itts.core;

import com.google.common.collect.ImmutableList;
import dev.felnull.itts.core.audio.VoiceAudioManager;
import dev.felnull.itts.core.cache.CacheManager;
import dev.felnull.itts.core.config.ConfigManager;
import dev.felnull.itts.core.dict.DictionaryManager;
import dev.felnull.itts.core.discord.Bot;
import dev.felnull.itts.core.savedata.SaveDataManager;
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
     * セーブデータマネージャー
     */
    private final SaveDataManager saveDataManager;

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
        this.saveDataManager = new SaveDataManager(runtimeContext.getSaveDataAccess());
        this.cacheManager = new CacheManager(runtimeContext.getGlobalCacheAccessFactory());

        this.managers = ImmutableList.of(configManager, saveDataManager, voiceManager);
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

        logger.info("Setup complete");

        bot.start();
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

    public SaveDataManager getSaveDataManager() {
        return saveDataManager;
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
}
