package dev.felnull.ttsvoice.core;

import dev.felnull.ttsvoice.core.discord.TTSVoiceBot;
import org.jetbrains.annotations.NotNull;

public class TTSVoiceRuntime {
    private final TTSVoiceBot bot;

    private TTSVoiceRuntime(@NotNull String botToken) {
        this.bot = new TTSVoiceBot(botToken);
    }

    public static TTSVoiceRuntime create(@NotNull String botToken) {
        return new TTSVoiceRuntime(botToken);
    }

    public void run() {
        bot.init();
    }
}
