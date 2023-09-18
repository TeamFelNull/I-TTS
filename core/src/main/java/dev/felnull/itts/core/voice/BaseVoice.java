package dev.felnull.itts.core.voice;

/**
 * 声のベース
 *
 * @author MORIMORI0317
 */
public abstract class BaseVoice implements Voice {
    /**
     * 声タイプ
     */
    protected final VoiceType voiceType;

    /**
     * コンストラクタ
     *
     * @param voiceType 声タイプ
     */
    protected BaseVoice(VoiceType voiceType) {
        this.voiceType = voiceType;
    }

    @Override
    public boolean isAvailable() {
        return voiceType.isAvailable();
    }

    @Override
    public VoiceType getVoiceType() {
        return voiceType;
    }
}
