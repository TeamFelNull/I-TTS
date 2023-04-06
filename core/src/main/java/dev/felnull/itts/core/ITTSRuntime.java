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

public class ITTSRuntime {
    private static ITTSRuntime INSTANCE;
    private final Logger logger;
    private final ExecutorService asyncWorkerExecutor = Executors.newCachedThreadPool(new BasicThreadFactory.Builder().namingPattern("async-worker-%d").daemon(true).build());
    private final ExecutorService heavyProcessExecutor = Executors.newFixedThreadPool(Math.max(Runtime.getRuntime().availableProcessors(), 1), new BasicThreadFactory.Builder().namingPattern("heavy-process-thread-%d").daemon(true).build());
    private final Timer timer = new Timer("ikisugi-timer", true);
    private final DirectoryLock directoryLock = new DirectoryLock();
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
    private final List<ITTSBaseManager> managers;
    private long startupTime;

    private ITTSRuntime(ITTSRuntimeContext runtimeContext) {
        if (INSTANCE != null)
            throw new IllegalStateException("ITTSRuntime must be a singleton instance");
        INSTANCE = this;

        directoryLock.lock();

        this.logger = runtimeContext.getLogContext().getLogger();

        var v = ITTSRuntime.class.getPackage().getImplementationVersion();
        this.developmentEnvironment = v == null;
        this.version = Objects.requireNonNullElse(v, "None");

        this.bot = new Bot();
        this.configManager = new ConfigManager(runtimeContext.getConfigContext());
        this.saveDataManager = new SaveDataManager(runtimeContext.getSaveDataAccess());
        this.cacheManager = new CacheManager(runtimeContext.getGlobalCacheAccessFactory());

        this.managers = ImmutableList.of(configManager, saveDataManager, voiceManager);
    }

    public static ITTSRuntime getInstance() {
        if (INSTANCE == null)
            throw new IllegalStateException("Instance does not exist");

        return INSTANCE;
    }

    public static ITTSRuntime newRuntime(@NotNull ITTSRuntimeContext runtimeContext) {
        return new ITTSRuntime(runtimeContext);
    }

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

    public Bot getBot() {
        return bot;
    }
}
