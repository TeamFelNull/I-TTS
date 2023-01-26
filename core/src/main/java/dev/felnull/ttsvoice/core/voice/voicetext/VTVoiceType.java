package dev.felnull.ttsvoice.core.voice.voicetext;

import com.google.common.base.CaseFormat;
import dev.felnull.ttsvoice.core.TTSVoiceRuntime;
import dev.felnull.ttsvoice.core.voice.VoiceCategory;
import dev.felnull.ttsvoice.core.voice.VoiceType;

public record VTVoiceType(VoiceTextSpeakers speakers) implements VoiceType {
    @Override
    public String getName() {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, speakers.getId()) + "(" + speakers.getName() + ")";
    }

    @Override
    public String getId() {
        return speakers.getId();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public VoiceCategory getCategory() {
        return TTSVoiceRuntime.getInstance().getVoiceManager().getVoiceTextManager().getCategory();
    }
}
