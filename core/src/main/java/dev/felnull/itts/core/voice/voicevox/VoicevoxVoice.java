package dev.felnull.itts.core.voice.voicevox;

import dev.felnull.itts.core.voice.CachedVoice;
import dev.felnull.itts.core.voice.VoiceType;

import java.io.IOException;
import java.io.InputStream;

/**
 * VOICEVOXの声
 *
 * @author MORIMORI0317
 */
public class VoicevoxVoice extends CachedVoice {
    /**
     * VOICEVOXマネージャー
     */
    private final VoicevoxManager manager;

    /**
     * 話者
     */
    private final VoicevoxSpeaker speaker;

    /**
     * 話者スタイル
     */
    private final VoicevoxStyle style;

    /**
     * コンストラクタ
     *
     * @param voiceType 声タイプ
     * @param manager   マネージャー
     * @param speaker   話者
     * @param style      話者スタイル
     */
    protected VoicevoxVoice(VoiceType voiceType, VoicevoxManager manager, VoicevoxSpeaker speaker, VoicevoxStyle style) {
        super(voiceType);
        this.manager = manager;
        this.speaker = speaker;
        this.style = style;
    }

    @Override
    protected InputStream openVoiceStream(String text) throws IOException, InterruptedException {
        return this.manager.openVoiceStream(text, style.id());
    }

    @Override
    protected String createHashCodeChars() {
        return this.speaker.uuid() + ":" + this.style.id();
    }
}
