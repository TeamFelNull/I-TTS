package dev.felnull.itts.core.config.voicetype;

import java.util.List;

/**
 * OpenAI互換TTSのコンフィグ
 */
public interface OpenAiTtsConfig extends VoiceTypeConfig {
    @Override
    default boolean isEnable() {
        return false;
    }

    /**
     * APIのベースURLを取得
     *
     * @return /audio/speechを追加するベースURL
     */
    default String getBaseUrl() {
        return "https://api.openai.com/v1";
    }

    /**
     * APIキーを取得
     *
     * @return APIキー、認証不要の場合は空文字列
     */
    default String getApiKey() {
        return "";
    }

    /**
     * モデル名を取得
     *
     * @return モデル名
     */
    default String getModel() {
        return "tts-1";
    }

    /**
     * 話者一覧を取得
     *
     * @return APIへ送信する話者名の一覧
     */
    default List<String> getVoices() {
        return List.of("alloy");
    }
}
