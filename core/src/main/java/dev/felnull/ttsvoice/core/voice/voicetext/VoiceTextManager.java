package dev.felnull.ttsvoice.core.voice.voicetext;

import dev.felnull.ttsvoice.core.voice.VoiceType;

import java.util.Arrays;
import java.util.List;

public class VoiceTextManager {
    private final VTVoiceCategory category = new VTVoiceCategory();
    private final List<VoiceType> voiceTypes = Arrays.stream(VoiceTextSpeakers.values()).map(VTVoiceType::new).map(t -> (VoiceType) t).toList();

    public VTVoiceCategory getCategory() {
        return category;
    }

    public List<VoiceType> getVoiceTypes() {
        return voiceTypes;
    }
}
