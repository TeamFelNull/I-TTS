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
     * 話者スタイル
     */
    private final CoeiroinkStyle coeiroinkStyle;

    /**
     * 互換性のため従来IDを使うかどうか
     */
    private final boolean legacyId;

    /**
     * マネージャー
     */
    private final CoeiroinkManager manager;

    /**
     * コンストラクタ
     *
     * @param coeiroinkSpeaker 話者
     * @param coeiroinkStyle   話者スタイル
     * @param coeiroinkManager マネージャー
     * @param legacyId         従来IDを使うかどうか
     */
    public CoeiroinkVoiceType(CoeiroinkSpeaker coeiroinkSpeaker, CoeiroinkStyle coeiroinkStyle, CoeiroinkManager coeiroinkManager, boolean legacyId) {
        this.coeiroinkSpeaker = coeiroinkSpeaker;
        this.coeiroinkStyle = coeiroinkStyle;
        this.manager = coeiroinkManager;
        this.legacyId = legacyId;
    }

    @Override
    public String getName() {
        return this.legacyId ? this.coeiroinkSpeaker.speakerName() : this.coeiroinkSpeaker.speakerName() + " " + this.coeiroinkStyle.styleName();
    }

    @Override
    public String getId() {
        String baseId = manager.getName() + "-" + this.coeiroinkSpeaker.speakerUuid();
        return this.legacyId ? baseId : baseId + "-" + this.coeiroinkStyle.styleId();
    }

    @Override
    public String getModelName() {
        return this.coeiroinkSpeaker.speakerName();
    }

    @Override
    public String getModelId() {
        return manager.getName() + "-" + this.coeiroinkSpeaker.speakerUuid();
    }

    @Override
    public String getStyleName() {
        return this.coeiroinkStyle.styleName();
    }

    @Override
    public String getStyleId() {
        return String.valueOf(this.coeiroinkStyle.styleId());
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
        return new CoeiroinkVoice(this, manager, coeiroinkSpeaker, coeiroinkStyle);
    }

    @Override
    public String getStatisticsName() {
        return coeiroinkSpeaker.speakerName();
    }
}
