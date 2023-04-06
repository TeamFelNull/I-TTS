package dev.felnull.itts.core.tts.saidtext;

import dev.felnull.itts.core.voice.Voice;

import java.util.concurrent.CompletableFuture;

public record LiteralSaidText(Voice voice, String text) implements SaidText {
    @Override
    public CompletableFuture<String> getText() {
        return CompletableFuture.completedFuture(text);
    }

    @Override
    public CompletableFuture<Voice> getVoice() {
        return CompletableFuture.completedFuture(voice);
    }
}
