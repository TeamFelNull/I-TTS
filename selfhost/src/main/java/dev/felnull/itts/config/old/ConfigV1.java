package dev.felnull.itts.config.old;

import blue.endless.jankson.JsonObject;
import com.google.common.collect.ImmutableList;
import dev.felnull.itts.config.ConfigLoader;
import dev.felnull.itts.core.config.DataBaseConfig.DataBaseType;
import dev.felnull.itts.core.util.NameSerializableEnum;
import dev.felnull.itts.utils.Json5Utils;

import java.util.List;
import java.util.Optional;

/**
 * バージョン1のコンフィグ
 *
 * @param botToken        BOTトークン
 * @param themeColor      テーマカラー
 * @param cacheTime       キャッシュを保持する期間
 * @param voiceTextConfig VOICETEXT コンフィグ
 * @param voicevoxConfig  VOICEVOX コンフィグ
 * @param coeirolnkConfig COEIROLNK コンフィグ
 * @param sharevoxConfig  SHAREVOX コンフィグ
 * @param dataBaseConfig  データベースコンフィグ
 */
public record ConfigV1(
        String botToken,
        int themeColor,
        long cacheTime,
        VoiceTextConfigV1 voiceTextConfig,
        VoicevoxConfigV1 voicevoxConfig,
        VoicevoxConfigV1 coeirolnkConfig,
        VoicevoxConfigV1 sharevoxConfig,
        DataBaseConfigV1 dataBaseConfig
) {

    /**
     * コンフィグローダー
     */
    public static final ConfigLoader<ConfigV1> LOADER = new ConfigLoader<>() {
        @Override
        public ConfigV1 load(JsonObject json5) {
            String botToken = Json5Utils.getStringOrElse(json5, "bot_token", "");
            int themeColor = json5.getInt("theme_color", 0xFF00FF);
            long cacheTime = json5.getLong("cache_time", 180000);
            VoiceTextConfigV1 voiceTextConfig = VoiceTextConfigV1.fromJson(Optional.ofNullable(json5.getObject("voice_text")).orElseGet(JsonObject::new));
            VoicevoxConfigV1 voicevoxConfig = VoicevoxConfigV1.fromJson(Optional.ofNullable(json5.getObject("voicevox")).orElseGet(JsonObject::new));
            VoicevoxConfigV1 coeirolnkConfig = VoicevoxConfigV1.fromJson(Optional.ofNullable(json5.getObject("coeirolnk")).orElseGet(JsonObject::new));
            VoicevoxConfigV1 sharevoxConfig = VoicevoxConfigV1.fromJson(Optional.ofNullable(json5.getObject("sharevox")).orElseGet(JsonObject::new));
            DataBaseConfigV1 dataBaseConfig = DataBaseConfigV1.fromJson(Optional.ofNullable(json5.getObject("data_base")).orElseGet(JsonObject::new));

            return new ConfigV1(
                    botToken,
                    themeColor,
                    cacheTime,
                    voiceTextConfig,
                    voicevoxConfig,
                    coeirolnkConfig,
                    sharevoxConfig,
                    dataBaseConfig
            );
        }

        @Override
        public ConfigV1 migrate(Object oldConfig) {
            ConfigV0 configV0 = (ConfigV0) oldConfig;

            return new ConfigV1(
                    configV0.botToken(),
                    configV0.themeColor(),
                    configV0.cacheTime(),
                    new VoiceTextConfigV1(configV0.voiceTextConfig().enable(), configV0.voiceTextConfig().apiKey()),
                    new VoicevoxConfigV1(configV0.voicevoxConfig().enable(), configV0.voicevoxConfig().apiUrls(), configV0.voicevoxConfig().checkTime()),
                    new VoicevoxConfigV1(configV0.coeirolnkConfig().enable(), configV0.coeirolnkConfig().apiUrls(), configV0.coeirolnkConfig().checkTime()),
                    new VoicevoxConfigV1(configV0.sharevoxConfig().enable(), configV0.sharevoxConfig().apiUrls(), configV0.sharevoxConfig().checkTime()),
                    new DataBaseConfigV1(DataBaseType.SQLITE, "", 0, "", "", "")
            );
        }
    };

    /**
     * VOICETEXTのコンフィグ
     *
     * @param enable 有効かどうか
     * @param apiKey APIキー
     */
    public record VoiceTextConfigV1(boolean enable, String apiKey) {

        /**
         * JSONを読み込んでコンフィグを作成
         *
         * @param jo JSONオブジェクト
         * @return コンフィグ
         */
        public static VoiceTextConfigV1 fromJson(JsonObject jo) {
            boolean enable = jo.getBoolean("enable", true);
            String apiKey = Json5Utils.getStringOrElse(jo, "api_key", "");
            return new VoiceTextConfigV1(enable, apiKey);
        }
    }

    /**
     * VOICEVOX系のコンフィグ
     *
     * @param enable    有効かどうか
     * @param apiUrls   APIのURLリスト
     * @param checkTime 接続確認の間隔
     */
    public record VoicevoxConfigV1(boolean enable, List<String> apiUrls, long checkTime) {

        /**
         * JSONを読み込んでコンフィグを作成
         *
         * @param jo JSONオブジェクト
         * @return コンフィグ
         */
        public static VoicevoxConfigV1 fromJson(JsonObject jo) {
            boolean enable = jo.getBoolean("enable", true);

            List<String> loadApiUrls = Json5Utils.getStringListOfJsonArray(jo, "api_url");
            List<String> apiUrls = loadApiUrls.isEmpty() ? ImmutableList.of("") : loadApiUrls;

            long checkTime = jo.getLong("check_time", 15000);
            return new VoicevoxConfigV1(enable, apiUrls, checkTime);
        }
    }

    /**
     * データベースのコンフィグ
     *
     * @param type         データベースの種類
     * @param host         ホスト名
     * @param port         ポート番号
     * @param databaseName データベース名
     * @param user         ユーザー名
     * @param password     パスワード
     */
    public record DataBaseConfigV1(
            DataBaseType type,
            String host,
            int port,
            String databaseName,
            String user,
            String password
    ) {

        /**
         * JSONを読み込んでコンフィグを作成
         *
         * @param jo JSONオブジェクト
         * @return コンフィグ
         */
        public static DataBaseConfigV1 fromJson(JsonObject jo) {
            String typeText = Json5Utils.getStringOrElse(jo, "type", DataBaseType.SQLITE.getName());
            DataBaseType type = NameSerializableEnum.getByName(DataBaseType.class, typeText, DataBaseType.SQLITE);
            String host = Json5Utils.getStringOrElse(jo, "host", "");
            int port = Json5Utils.getInt(jo, "port");
            String databaseName = Json5Utils.getStringOrElse(jo, "database_name", "");
            String user = Json5Utils.getStringOrElse(jo, "user", "");
            String password = Json5Utils.getStringOrElse(jo, "password", "");

            return new DataBaseConfigV1(type, host, port, databaseName, user, password);
        }
    }
}
