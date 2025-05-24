package dev.felnull.itts.core.voice.coeiroink;

import dev.felnull.itts.core.voice.CachedVoice;
import dev.felnull.itts.core.voice.VoiceType;

import java.io.IOException;
import java.io.InputStream;

/**
 * VOICEVOXの声
 *
 * @author MORIMORI0317
 */
public class CoeiroinkVoice extends CachedVoice {
    /**
     * Coeiroinkマネージャー
     */
    private final CoeiroinkManager manager;

    /**
     * 話者
     */
    private final CoeiroinkSpeaker speaker;

    /**
     * コンストラクタ
     *
     * @param voiceType 声タイプ
     * @param manager   マネージャー
     * @param speaker   話者
     */
    protected CoeiroinkVoice(VoiceType voiceType, CoeiroinkManager manager, CoeiroinkSpeaker speaker) {
        super(voiceType);
        this.manager = manager;
        this.speaker = speaker;
    }

    @Override
    protected InputStream openVoiceStream(String text) throws IOException, InterruptedException {
        return this.manager.openVoiceStream(text, speaker.styles().get(0).styleId(), speaker.speakerUuid().toString());
    }

    @Override
    protected String createHashCodeChars() {
        return this.speaker.speakerUuid().toString();
    }
}
