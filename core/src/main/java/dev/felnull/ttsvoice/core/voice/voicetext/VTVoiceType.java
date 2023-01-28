package dev.felnull.ttsvoice.core.voice.voicetext;

import com.google.common.base.CaseFormat;
import dev.felnull.ttsvoice.core.TTSVoiceRuntime;
import dev.felnull.ttsvoice.core.voice.CachedVoiceType;
import dev.felnull.ttsvoice.core.voice.VoiceCategory;

import java.io.IOException;
import java.io.InputStream;

public class VTVoiceType extends CachedVoiceType {
    private final VoiceTextSpeaker speakers;

    public VTVoiceType(VoiceTextSpeaker speakers) {
        this.speakers = speakers;
    }

    @Override
    public String getName() {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, speakers.getId()) + " (" + speakers.getName() + ")";
    }

    @Override
    public String getId() {
        return speakers.getId();
    }

    @Override
    public boolean isAvailable() {
        return getCategory().isAvailable();
    }

    @Override
    public VoiceCategory getCategory() {
        return getVoiceTextManager().getCategory();
    }

    @Override
    public InputStream openVoiceStream(String text) throws IOException, InterruptedException {
        return getVoiceTextManager().getVoice(speakers, text);
    }


    private VoiceTextManager getVoiceTextManager() {
        return TTSVoiceRuntime.getInstance().getVoiceManager().getVoiceTextManager();
    }
}
