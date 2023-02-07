package dev.felnull.itts.core;

import dev.felnull.itts.core.audio.VoiceAudioManager;
import dev.felnull.itts.core.cache.CacheManager;
import dev.felnull.itts.core.cache.GlobalCacheAccess;
import dev.felnull.itts.core.config.ConfigAccess;
import dev.felnull.itts.core.config.ConfigManager;
import dev.felnull.itts.core.dict.DictionaryManager;
import dev.felnull.itts.core.discord.Bot;
import dev.felnull.itts.core.savedata.SaveDataAccess;
import dev.felnull.itts.core.savedata.SaveDataManager;
import dev.felnull.itts.core.tts.TTSManager;
import dev.felnull.itts.core.voice.VoiceManager;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Timer;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class TTSVoiceRuntime {
    private static TTSVoiceRuntime INSTANCE;
    private final Logger logger = LogManager.getLogger(TTSVoiceRuntime.class);
    private final ExecutorService asyncWorkerExecutor = Executors.newCachedThreadPool(new BasicThreadFactory.Builder().namingPattern("async-worker-%d").daemon(true).build());
    private final ExecutorService heavyProcessExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new BasicThreadFactory.Builder().namingPattern("heavy-process-thread-%d").daemon(true).build());
    private final Timer timer = new Timer("ikisugi-timer", true);
    private final String version;
    private final boolean developmentEnvironment;
    private final ConfigManager configManager;
    private final TTSManager ttsManager = new TTSManager();
    private final VoiceManager voiceManager = new VoiceManager();
    private final VoiceAudioManager voiceAudioManager = new VoiceAudioManager();
    private final DictionaryManager dictionaryManager = new DictionaryManager();
    private final CacheManager cacheManager;
    private final SaveDataManager saveDataManager;
    private final Bot bot;
    private long startupTime;

    private TTSVoiceRuntime(@NotNull ConfigAccess configAccess, @NotNull SaveDataAccess saveDataAccess, @Nullable Supplier<GlobalCacheAccess> globalCacheAccessFactory) {
        if (INSTANCE != null)
            throw new IllegalStateException("TTSVoiceRuntime must be a singleton instance/TTSVoiceRuntimeはシングルトンインスタンスである必要があります");
        INSTANCE = this;

        var v = TTSVoiceRuntime.class.getPackage().getImplementationVersion();
        this.developmentEnvironment = v == null;
        this.version = Objects.requireNonNullElse(v, "None");

        this.bot = new Bot();
        this.configManager = new ConfigManager(configAccess);
        this.saveDataManager = new SaveDataManager(saveDataAccess);
        this.cacheManager = new CacheManager(globalCacheAccessFactory);
    }

    public static TTSVoiceRuntime getInstance() {
        if (INSTANCE == null)
            throw new IllegalStateException("Instance does not exist/インスタンスが存在しません");

        return INSTANCE;
    }

    public static TTSVoiceRuntime newRuntime(@NotNull ConfigAccess configAccess, @NotNull SaveDataAccess saveDataAccess, @Nullable Supplier<GlobalCacheAccess> globalCacheAccessFactory) {
        return new TTSVoiceRuntime(configAccess, saveDataAccess, globalCacheAccessFactory);
    }

    public void execute() {
        startupTime = System.currentTimeMillis();

        logger.info("The Ikisugi TTS ({})", getVersionText());

        logger.info("--System info--");
        logger.info("Java runtime: {}", System.getProperty("java.runtime.name"));
        logger.info("Java version: {}", System.getProperty("java.version"));
        logger.info("Java vm name: {}", System.getProperty("java.vm.name"));
        logger.info("Java vm version: {}", System.getProperty("java.vm.version"));
        logger.info("OS: {}", System.getProperty("os.name"));
        logger.info("Arch: {}", System.getProperty("os.arch"));
        logger.info("Available Processors: {}", Runtime.getRuntime().availableProcessors());
        logger.info("---------------");

        if (!configManager.init()) {
            System.exit(1);
            return;
        }

        if (!saveDataManager.init()) {
            System.exit(1);
            return;
        }

        voiceManager.init();

        bot.init();
    }

    public long getStartupTime() {
        return startupTime;
    }

    /**
     * 軽い非同期処理を行うためのエクスキューター
     *
     * @return エクスキューター
     */
    public Executor getAsyncWorkerExecutor() {
        return asyncWorkerExecutor;
    }

    /**
     * CPUを利用する処理、大量のIOなどの重い処理を行うエクスキューター
     *
     * @return エクスキューター
     */
    public ExecutorService getHeavyProcessExecutor() {
        return heavyProcessExecutor;
    }

    public Timer getTimer() {
        return timer;
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
}
