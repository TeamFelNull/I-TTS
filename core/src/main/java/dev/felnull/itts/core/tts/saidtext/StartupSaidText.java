package dev.felnull.itts.core.tts.saidtext;

import dev.felnull.itts.core.ITTSRuntimeUse;
import dev.felnull.itts.core.util.DiscordUtils;
import dev.felnull.itts.core.voice.Voice;

public record StartupSaidText(Voice voice) implements SaidText, ITTSRuntimeUse {
    @Override
    public String getText() {
        String name = DiscordUtils.getName(getBot().getJDA().getSelfUser());
        return name + "が起動しました";
    }

    @Override
    public Voice getVoice() {
        return voice;
    }
}
