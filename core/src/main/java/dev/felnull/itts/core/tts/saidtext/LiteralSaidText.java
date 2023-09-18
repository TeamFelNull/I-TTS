package dev.felnull.itts.core.tts.saidtext;

import dev.felnull.itts.core.voice.Voice;

import java.util.concurrent.CompletableFuture;

/**
 * 文字列の読み上げテキスト
 *
 * @param voice 音声タイプ
 * @param text  読み上げるテキスト
 * @author MORIMORI0317
 */
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
