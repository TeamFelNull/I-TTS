package dev.felnull.itts.core.tts.saidtext;

import dev.felnull.itts.core.ITTSRuntimeUse;
import dev.felnull.itts.core.util.DiscordUtils;
import dev.felnull.itts.core.voice.Voice;

import java.util.concurrent.CompletableFuture;

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
