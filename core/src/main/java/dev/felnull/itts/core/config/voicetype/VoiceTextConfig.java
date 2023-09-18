package dev.felnull.itts.core.config.voicetype;

import org.jetbrains.annotations.NotNull;

/**
 * VoiceTextのコンフィグ
 *
 * @author MORIMORI0317
 */
public interface VoiceTextConfig extends VoiceTypeConfig {
    /**
     * デフォルトのAPIキー
     */
    String DEFAULT_API_KEY = "";

    /**
     * APIキーを取得
     *
     * @return APIキー
     */
    @NotNull
    String getApiKey();
}
