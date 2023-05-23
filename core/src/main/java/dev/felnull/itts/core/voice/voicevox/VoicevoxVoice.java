package dev.felnull.itts.core.voice.voicevox;

import dev.felnull.itts.core.voice.CachedVoice;
import dev.felnull.itts.core.voice.VoiceType;

import java.io.IOException;
import java.io.InputStream;

public class VoicevoxVoice extends CachedVoice {
    private final VoicevoxManager manager;
    private final VoicevoxSpeaker speaker;

    protected VoicevoxVoice(VoiceType voiceType, VoicevoxManager manager, VoicevoxSpeaker speaker) {
        super(voiceType);
        this.manager = manager;
        this.speaker = speaker;
    }

    @Override
    protected InputStream openVoiceStream(String text) throws IOException, InterruptedException {
        return this.manager.openVoiceStream(text, speaker.styles().get(0).id());
    }

    @Override
    protected String createHashCodeChars() {
        return this.speaker.uuid().toString();
    }
}
