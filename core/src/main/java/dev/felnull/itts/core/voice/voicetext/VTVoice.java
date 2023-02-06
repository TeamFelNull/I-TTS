package dev.felnull.itts.core.voice.voicetext;

import dev.felnull.itts.core.TTSVoiceRuntime;
import dev.felnull.itts.core.voice.CachedVoice;
import dev.felnull.itts.core.voice.VoiceType;

import java.io.IOException;
import java.io.InputStream;

public class VTVoice extends CachedVoice {
    private final VoiceTextSpeaker speakers;

    protected VTVoice(VoiceType voiceType, VoiceTextSpeaker speakers) {
        super(voiceType);
        this.speakers = speakers;
    }

    @Override
    protected InputStream openVoiceStream(String text) throws IOException, InterruptedException {
        return getVoiceTextManager().getVoice(speakers, text);
    }

    @Override
    protected String createHashCodeChars() {
        return speakers.getId();
    }

    private VoiceTextManager getVoiceTextManager() {
        return TTSVoiceRuntime.getInstance().getVoiceManager().getVoiceTextManager();
    }
}
