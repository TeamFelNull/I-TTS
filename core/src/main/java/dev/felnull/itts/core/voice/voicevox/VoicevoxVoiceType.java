package dev.felnull.itts.core.voice.voicevox;

import dev.felnull.itts.core.voice.Voice;
import dev.felnull.itts.core.voice.VoiceCategory;
import dev.felnull.itts.core.voice.VoiceType;

/**
 * VOICEVOXの声タイプ
 *
 * @author MORIMORI0317
 */
public class VoicevoxVoiceType implements VoiceType {

    /**
     * 話者
     */
    private final VoicevoxSpeaker voicevoxSpeaker;

    /**
     * 話者スタイル
     */
    private final VoicevoxStyle voicevoxStyle;

    /**
     * 互換性のため従来IDを使うかどうか
     */
    private final boolean legacyId;

    /**
     * マネージャー
     */
    private final VoicevoxManager manager;

    /**
     * コンストラクタ
     *
     * @param voicevoxSpeaker 話者
     * @param voicevoxStyle   話者スタイル
     * @param voicevoxManager マネージャー
     * @param legacyId        従来IDを使うかどうか
     */
    public VoicevoxVoiceType(VoicevoxSpeaker voicevoxSpeaker, VoicevoxStyle voicevoxStyle, VoicevoxManager voicevoxManager, boolean legacyId) {
        this.voicevoxSpeaker = voicevoxSpeaker;
        this.voicevoxStyle = voicevoxStyle;
        this.manager = voicevoxManager;
        this.legacyId = legacyId;
    }

    @Override
    public String getName() {
        return this.legacyId ? this.voicevoxSpeaker.name() : this.voicevoxSpeaker.name() + " " + this.voicevoxStyle.name();
    }

    @Override
    public String getId() {
        String baseId = manager.getName() + "-" + this.voicevoxSpeaker.uuid();
        return this.legacyId ? baseId : baseId + "-" + this.voicevoxStyle.id();
    }

    @Override
    public String getModelName() {
        return this.voicevoxSpeaker.name();
    }

    @Override
    public String getModelId() {
        return manager.getName() + "-" + this.voicevoxSpeaker.uuid();
    }

    @Override
    public String getStyleName() {
        return this.voicevoxStyle.name();
    }

    @Override
    public String getStyleId() {
        return String.valueOf(this.voicevoxStyle.id());
    }

    @Override
    public boolean isAvailable() {
        return manager.isAvailable() && manager.getBalancer().getAvailableSpeakers().contains(voicevoxSpeaker);
    }

    @Override
    public VoiceCategory getCategory() {
        return manager.getCategory();
    }

    @Override
    public String getStatisticsName() {
        return voicevoxSpeaker.name();
    }

    @Override
    public Voice createVoice(long guildId, long userId) {
        return new VoicevoxVoice(this, manager, voicevoxSpeaker, voicevoxStyle);
    }
}
