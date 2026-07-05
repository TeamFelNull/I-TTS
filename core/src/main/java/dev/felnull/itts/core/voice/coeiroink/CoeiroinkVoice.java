package dev.felnull.itts.core.voice.coeiroink;

import dev.felnull.itts.core.voice.CachedVoice;
import dev.felnull.itts.core.voice.VoiceType;

import java.io.IOException;
import java.io.InputStream;

/**
 * Coeiroinkの声
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
     * 話者スタイル
     */
    private final CoeiroinkStyle style;

    /**
     * コンストラクタ
     *
     * @param voiceType 声タイプ
     * @param manager   マネージャー
     * @param speaker   話者
     * @param style     話者スタイル
     */
    protected CoeiroinkVoice(VoiceType voiceType, CoeiroinkManager manager, CoeiroinkSpeaker speaker, CoeiroinkStyle style) {
        super(voiceType);
        this.manager = manager;
        this.speaker = speaker;
        this.style = style;
    }

    @Override
    protected InputStream openVoiceStream(String text) throws IOException, InterruptedException {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Text cannot be null or empty");
        }

        return this.manager.openVoiceStream(text, style.styleId(), speaker.speakerUuid().toString());
    }

    @Override
    protected String createHashCodeChars() {
        return this.speaker.speakerUuid() + ":" + this.style.styleId();
    }
}
