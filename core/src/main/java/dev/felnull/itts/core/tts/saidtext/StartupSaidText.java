package dev.felnull.itts.core.tts.saidtext;

import dev.felnull.itts.core.ITTSRuntimeUse;
import dev.felnull.itts.core.voice.Voice;

import java.util.concurrent.CompletableFuture;

/**
 * 起動時の読み上げテキスト
 *
 * @param voice 音声タイプ
 * @author MORIMORI0317
 */
public record StartupSaidText(Voice voice) implements SaidText, ITTSRuntimeUse {
    @Override
    public CompletableFuture<String> getText() {
        return CompletableFuture.supplyAsync(() -> {
            String name = getBot().getJDA().getSelfUser().getName();
            return name + "が起動しました";
        }, getAsyncExecutor());
    }

    @Override
    public CompletableFuture<Voice> getVoice() {
        return CompletableFuture.completedFuture(voice);
    }
}
