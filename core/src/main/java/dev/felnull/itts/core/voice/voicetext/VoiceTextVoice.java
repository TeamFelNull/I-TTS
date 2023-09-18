package dev.felnull.itts.core.voice.voicetext;

import dev.felnull.itts.core.ITTSRuntime;
import dev.felnull.itts.core.voice.CachedVoice;
import dev.felnull.itts.core.voice.VoiceType;

import java.io.IOException;
import java.io.InputStream;

/**
 * VoiceTextの声
 *
 * @author MORIMORI0317
 */
public class VoiceTextVoice extends CachedVoice {

    /**
     * 話者
     */
    private final VoiceTextSpeaker speakers;

    /**
     * コンストラクタ
     *
     * @param voiceType 声タイプ
     * @param speakers  話者
     */
    protected VoiceTextVoice(VoiceType voiceType, VoiceTextSpeaker speakers) {
        super(voiceType);
        this.speakers = speakers;
    }

    @Override
    protected InputStream openVoiceStream(String text) throws IOException, InterruptedException {
        return getVoiceTextManager().openVoiceStream(speakers, text);
    }

    @Override
    protected String createHashCodeChars() {
        return speakers.getId();
    }

    private VoiceTextManager getVoiceTextManager() {
        return ITTSRuntime.getInstance().getVoiceManager().getVoiceTextManager();
    }

    @Override
    public int getReadLimit() {
        return 180;
    }
}
