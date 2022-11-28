package dev.felnull.ttsvoice.core;

import dev.felnull.ttsvoice.core.discord.Bot;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TTSVoiceRuntime {
    private final ExecutorService asyncWorkerExecutor = Executors.newCachedThreadPool(new BasicThreadFactory.Builder().namingPattern("async-worker-thread-%d").daemon(true).build());
    private final Bot bot;
    private final boolean developmentEnvironment = true;

    private TTSVoiceRuntime(@NotNull String botToken) {
        this.bot = new Bot(this, botToken);
    }

    public static TTSVoiceRuntime create(@NotNull String botToken) {
        return new TTSVoiceRuntime(botToken);
    }

    public void run() {
        bot.init();
    }

    public Executor getAsyncWorkerExecutor() {
        return asyncWorkerExecutor;
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
}
