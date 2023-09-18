package dev.felnull.itts.core.config;

import dev.felnull.itts.core.config.voicetype.VoiceTextConfig;
import dev.felnull.itts.core.config.voicetype.VoicevoxConfig;
import org.jetbrains.annotations.NotNull;

/**
 * コンフィグ
 *
 * @author MORIMORI0317
 */
public interface Config {

    /**
     * コンフィグのバージョン
     */
    int VERSION = 0;

    /**
     * デフォルトのBOTトークン
     */
    String DEFAULT_BOT_TOKEN = "";

    /**
     * デフォルトのテーマカラー
     */
    int DEFAULT_THEME_COLOR = 0xFF00FF;

    /**
     * デフォルトのキャッシュ保持期間
     */
    long DEFAULT_CACHE_TIME = 180000;

    /**
     * BOTトークンを取得
     *
     * @return BOTトークン
     */
    @NotNull
    String getBotToken();

    /**
     * テーマカラーを取得
     *
     * @return テーマカラー
     */
    int getThemeColor();

    /**
     * キャッシュの保持期間取得
     *
     * @return キャッシュの保持期間
     */
    long getCacheTime();

    /**
     * VoiceTextのコンフィグを取得
     *
     * @return VoiceTextのコンフィグ
     */
    VoiceTextConfig getVoiceTextConfig();

    /**
     * VOICEVOXのコンフィグを取得
     *
     * @return VOICEVOXのコンフィグ
     */
    VoicevoxConfig getVoicevoxConfig();

    /**
     * COEIROLNKのコンフィグを取得
     *
     * @return COEIROLNKのコンフィグ
     */
    VoicevoxConfig getCoeirolnkConfig();

    /**
     * SHAREVOXのコンフィグを取得
     *
     * @return SHAREVOXのコンフィグ
     */
    VoicevoxConfig getSharevoxConfig();
}
