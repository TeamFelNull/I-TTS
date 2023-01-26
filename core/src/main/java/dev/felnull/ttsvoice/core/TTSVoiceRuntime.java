package dev.felnull.ttsvoice.core;

import dev.felnull.ttsvoice.core.audio.VoiceAudioManager;
import dev.felnull.ttsvoice.core.config.ConfigAccess;
import dev.felnull.ttsvoice.core.config.ConfigManager;
import dev.felnull.ttsvoice.core.discord.Bot;
import dev.felnull.ttsvoice.core.savedata.SaveDataAccess;
import dev.felnull.ttsvoice.core.savedata.SaveDataManager;
import dev.felnull.ttsvoice.core.tts.TTSManager;
import dev.felnull.ttsvoice.core.voice.VoiceManager;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TTSVoiceRuntime {
    private static TTSVoiceRuntime INSTANCE;
    private final Logger logger = LogManager.getLogger(TTSVoiceRuntime.class);
    private final ExecutorService asyncWorkerExecutor = Executors.newCachedThreadPool(new BasicThreadFactory.Builder().namingPattern("async-worker-thread-%d").daemon(true).build());
    private final ExecutorService heavyProcessExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new BasicThreadFactory.Builder().namingPattern("heavy-process-thread-%d").daemon(true).build());
    private final ConfigManager configManager;
    private final TTSManager ttsManager = new TTSManager();
    private final VoiceManager voiceManager = new VoiceManager();
    private final VoiceAudioManager voiceAudioManager = new VoiceAudioManager();
    private final SaveDataManager saveDataManager;
    private final Bot bot;
    private final boolean developmentEnvironment = true;

    private TTSVoiceRuntime(@NotNull ConfigAccess configAccess, @NotNull SaveDataAccess saveDataAccess) {
        if (INSTANCE != null)
            throw new IllegalStateException("TTSVoiceRuntime must be a singleton instance");
        INSTANCE = this;

        this.bot = new Bot(this);
        this.configManager = new ConfigManager(this, configAccess);
        this.saveDataManager = new SaveDataManager(this, saveDataAccess);
    }

    public static TTSVoiceRuntime getInstance() {
        if (INSTANCE == null)
            throw new IllegalStateException("Instance does not exist");

        return INSTANCE;
    }

    public static TTSVoiceRuntime newRuntime(@NotNull ConfigAccess configAccess, @NotNull SaveDataAccess saveDataAccess) {
        return new TTSVoiceRuntime(configAccess, saveDataAccess);
    }

    public void execute() {
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

    public boolean isDevelopmentEnvironment() {
        return developmentEnvironment;
    }

    public String getVersion() {
        return "2.0.0";
    }

    public String getVersionText() {
        if (isDevelopmentEnvironment())
            return "開発環境";
        return "v" + getVersion();
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
}
