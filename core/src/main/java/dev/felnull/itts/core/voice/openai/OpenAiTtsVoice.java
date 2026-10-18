package dev.felnull.itts.core.voice.openai;

import dev.felnull.itts.core.voice.CachedVoice;
import dev.felnull.itts.core.voice.VoiceType;

import java.io.IOException;
import java.io.InputStream;

/**
 * OpenAI互換TTSの音声
 */
public class OpenAiTtsVoice extends CachedVoice {
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
     * @param voiceType 声タイプ
     * @param manager   TTS管理
     * @param speaker   話者名
     */
    protected OpenAiTtsVoice(VoiceType voiceType, OpenAiTtsManager manager, String speaker) {
        super(voiceType);
        this.manager = manager;
        this.speaker = speaker;
    }

    @Override
    protected InputStream openVoiceStream(String text) throws IOException, InterruptedException {
        return manager.openVoiceStream(speaker, text);
    }

    @Override
    protected String createHashCodeChars() {
        return manager.getCacheIdentity();
    }

    @Override
    public int getReadLimit() {
        return 4096;
    }
}
