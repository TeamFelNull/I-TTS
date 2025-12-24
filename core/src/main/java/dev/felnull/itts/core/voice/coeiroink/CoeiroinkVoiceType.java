package dev.felnull.itts.core.voice.coeiroink;

import dev.felnull.itts.core.voice.Voice;
import dev.felnull.itts.core.voice.VoiceCategory;
import dev.felnull.itts.core.voice.VoiceType;

/**
 * Coeiroinkの声タイプ
 *
 * @author MORIMORI0317
 */
public class CoeiroinkVoiceType implements VoiceType {

    /**
     * 話者
     */
    private final CoeiroinkSpeaker coeiroinkSpeaker;

    /**
     * マネージャー
     */
    private final CoeiroinkManager manager;

    /**
     * コンストラクタ
     *
     * @param coeiroinkSpeaker 話者
     * @param coeiroinkManager マネージャー
     */
    public CoeiroinkVoiceType(CoeiroinkSpeaker coeiroinkSpeaker, CoeiroinkManager coeiroinkManager) {
        this.coeiroinkSpeaker = coeiroinkSpeaker;
        this.manager = coeiroinkManager;
    }

    @Override
    public String getName() {
        return this.coeiroinkSpeaker.speakerName();
    }

    @Override
    public String getId() {
        return manager.getName() + "-" + this.coeiroinkSpeaker.speakerUuid().toString();
    }

    @Override
    public boolean isAvailable() {
        return manager.isAvailable() && manager.getBalancer().getAvailableSpeakers().contains(coeiroinkSpeaker);
    }

    @Override
    public VoiceCategory getCategory() {
        return manager.getCategory();
    }

    @Override
    public Voice createVoice(long guildId, long userId) {
        return new CoeiroinkVoice(this, manager, coeiroinkSpeaker);
    }
}
