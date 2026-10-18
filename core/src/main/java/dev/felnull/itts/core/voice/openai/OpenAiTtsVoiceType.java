package dev.felnull.itts.core.voice.openai;

import dev.felnull.itts.core.voice.Voice;
import dev.felnull.itts.core.voice.VoiceCategory;
import dev.felnull.itts.core.voice.VoiceType;

/**
 * OpenAI互換TTSの声タイプ
 */
public class OpenAiTtsVoiceType implements VoiceType {
    /**
     * TTS管理
     */
    private final OpenAiTtsManager manager;

    /**
     * 話者名
     */
    private final String speaker;

    /**
     * コンストラクタ
     *
     * @param manager TTS管理
     * @param speaker 話者名
     */
    public OpenAiTtsVoiceType(OpenAiTtsManager manager, String speaker) {
        this.manager = manager;
        this.speaker = speaker;
    }

    @Override
    public String getName() {
        return speaker;
    }

    @Override
    public String getId() {
        return "openai_tts:" + speaker;
    }

    @Override
    public boolean isAvailable() {
        return manager.isAvailable();
    }

    @Override
    public VoiceCategory getCategory() {
        return manager.getCategory();
    }

    @Override
    public Voice createVoice(long guildId, long userId) {
        return new OpenAiTtsVoice(this, manager, speaker);
    }

    @Override
    public String getStatisticsName() {
        return getId();
    }
}
