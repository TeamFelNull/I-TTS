package dev.felnull.itts.core.voice.openai;

import dev.felnull.itts.core.voice.VoiceCategory;

/**
 * OpenAI互換TTSの声カテゴリ
 */
public class OpenAiTtsVoiceCategory implements VoiceCategory {
    /**
     * TTS管理
     */
    private final OpenAiTtsManager manager;

    /**
     * コンストラクタ
     *
     * @param manager TTS管理
     */
    public OpenAiTtsVoiceCategory(OpenAiTtsManager manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return "OpenAI互換TTS";
    }

    @Override
    public String getId() {
        return "openai_tts";
    }

    @Override
    public boolean isAvailable() {
        return manager.isAvailable();
    }
}
