package dev.felnull.itts.core.voice.voicetext;

import com.google.common.base.CaseFormat;
import dev.felnull.itts.core.ITTSRuntime;
import dev.felnull.itts.core.voice.Voice;
import dev.felnull.itts.core.voice.VoiceCategory;
import dev.felnull.itts.core.voice.VoiceType;

/**
 * VoiceTextの声タイプ
 *
 * @author MORIMORI0317
 */
public class VoiceTextVoiceType implements VoiceType {

    /**
     * 話者
     */
    private final VoiceTextSpeaker speakers;

    /**
     * コンストラクタ
     *
     * @param speakers 話者
     */
    public VoiceTextVoiceType(VoiceTextSpeaker speakers) {
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
    public Voice createVoice(long guildId, long userId) {
        return new VoiceTextVoice(this, speakers);
    }

    private VoiceTextManager getVoiceTextManager() {
        return ITTSRuntime.getInstance().getVoiceManager().getVoiceTextManager();
    }
}
