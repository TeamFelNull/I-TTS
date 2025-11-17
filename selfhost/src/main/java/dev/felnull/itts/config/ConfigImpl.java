package dev.felnull.itts.config;

import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import dev.felnull.itts.config.old.ConfigV0;
import dev.felnull.itts.core.config.Config;
import dev.felnull.itts.core.config.DataBaseConfig;
import dev.felnull.itts.core.config.voicetype.VoiceTextConfig;
import dev.felnull.itts.core.config.voicetype.VoicevoxConfig;
import dev.felnull.itts.core.util.NameSerializableEnum;
import dev.felnull.itts.utils.Json5Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Optional;

/**
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
public record ConfigImpl(
        String botToken,
        int themeColor,
        long cacheTime,
        VoiceTextConfig voiceTextConfig,
        VoicevoxConfig voicevoxConfig,
        VoicevoxConfig coeirolnkConfig,
        VoicevoxConfig sharevoxConfig,
        DataBaseConfig dataBaseConfig
) implements Config {

    /**
     * コンフィグローダー
     */
    public static final ConfigLoader<ConfigImpl> LOADER = new ConfigLoader<>() {
        @Override
        public ConfigImpl load(JsonObject json5) {
            String botToken = Json5Utils.getStringOrElse(json5, "bot_token", DEFAULT_BOT_TOKEN);
            int themeColor = json5.getInt("theme_color", DEFAULT_THEME_COLOR);
            long cacheTime = json5.getLong("cache_time", DEFAULT_CACHE_TIME);
            VoiceTextConfig voiceTextConfig = VoiceTextConfigImpl.fromJson(Optional.ofNullable(json5.getObject("voice_text")).orElseGet(JsonObject::new));
            VoicevoxConfig voicevoxConfig = VoicevoxConfigImpl.fromJson(Optional.ofNullable(json5.getObject("voicevox")).orElseGet(JsonObject::new));
            VoicevoxConfig coeirolnkConfig = VoicevoxConfigImpl.fromJson(Optional.ofNullable(json5.getObject("coeirolnk")).orElseGet(JsonObject::new));
            VoicevoxConfig sharevoxConfig = VoicevoxConfigImpl.fromJson(Optional.ofNullable(json5.getObject("sharevox")).orElseGet(JsonObject::new));
            DataBaseConfig dataBaseConfig = DataBaseConfigImpl.fromJson(Optional.ofNullable(json5.getObject("data_base")).orElseGet(JsonObject::new));

            return new ConfigImpl(
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
        public ConfigImpl migrate(Object oldConfig) {
            ConfigV0 configV0 = (ConfigV0) oldConfig;

            return new ConfigImpl(
                    configV0.botToken(),
                    configV0.themeColor(),
                    configV0.cacheTime(),
                    new VoiceTextConfigImpl(configV0.voiceTextConfig().enable(), configV0.voiceTextConfig().apiKey()),
                    new VoicevoxConfigImpl(configV0.voicevoxConfig().enable(), configV0.voicevoxConfig().apiUrls(), configV0.voicevoxConfig().checkTime()),
                    new VoicevoxConfigImpl(configV0.coeirolnkConfig().enable(), configV0.coeirolnkConfig().apiUrls(), configV0.coeirolnkConfig().checkTime()),
                    new VoicevoxConfigImpl(configV0.sharevoxConfig().enable(), configV0.sharevoxConfig().apiUrls(), configV0.sharevoxConfig().checkTime()),
                    new DataBaseConfigImpl()
            );
        }
    };

    /**
     * 初期化コンフィグ作成
     *
     * @return 初期コンフィグ
     */
    public static ConfigImpl createInitialConfig() {
        return new ConfigImpl(
                DEFAULT_BOT_TOKEN,
                DEFAULT_THEME_COLOR,
                DEFAULT_CACHE_TIME,
                new VoiceTextConfigImpl(),
                new VoicevoxConfigImpl(),
                new VoicevoxConfigImpl(),
                new VoicevoxConfigImpl(),
                new DataBaseConfigImpl()
        );
    }

    /**
     * Jsonへ書き込む
     */
    public void writeToJson(JsonObject json5) {
        json5.put("bot_token", JsonPrimitive.of(this.botToken), "BOTのトークン");
        json5.put("theme_color", new JsonPrimitive(this.themeColor), "テーマカラー");
        json5.put("cache_time", new JsonPrimitive(this.cacheTime), "キャッシュを保存する期間(ms)");
        json5.put("voice_text", ((VoiceTextConfigImpl) this.voiceTextConfig).toJson(), "VoiceTextのコンフィグ");
        json5.put("voicevox", ((VoicevoxConfigImpl) this.voicevoxConfig).toJson(), "VOICEVOXのコンフィグ");
        json5.put("coeirolnk", ((VoicevoxConfigImpl) this.coeirolnkConfig).toJson(), "COEIROLNKのコンフィグ");
        json5.put("sharevox", ((VoicevoxConfigImpl) this.sharevoxConfig).toJson(), "SHAREVOXのコンフィグ");
        json5.put("data_base", ((DataBaseConfigImpl) this.dataBaseConfig).toJson(), "データベースのコンフィグ");
    }

    @Override
    public @NotNull String getBotToken() {
        return botToken;
    }

    @Override
    public int getThemeColor() {
        return themeColor;
    }

    @Override
    public long getCacheTime() {
        return cacheTime;
    }

    @Override
    public VoiceTextConfig getVoiceTextConfig() {
        return voiceTextConfig;
    }

    @Override
    public VoicevoxConfig getVoicevoxConfig() {
        return voicevoxConfig;
    }

    @Override
    public VoicevoxConfig getCoeirolnkConfig() {
        return coeirolnkConfig;
    }

    @Override
    public VoicevoxConfig getSharevoxConfig() {
        return sharevoxConfig;
    }

    @Override
    public DataBaseConfig getDataBaseConfig() {
        return dataBaseConfig;
    }

    /**
     * VOICETEXTコンフィグの実装
     *
     * @param enable 有効かどうか
     * @param apiKey APIキー
     */
    private record VoiceTextConfigImpl(boolean enable, String apiKey) implements VoiceTextConfig {

        private VoiceTextConfigImpl() {
            this(DEFAULT_ENABLE, DEFAULT_API_KEY);
        }

        public static VoiceTextConfigImpl fromJson(JsonObject jo) {
            boolean enable = jo.getBoolean("enable", DEFAULT_ENABLE);
            String apiKey = Json5Utils.getStringOrElse(jo, "api_key", DEFAULT_API_KEY);
            return new VoiceTextConfigImpl(enable, apiKey);
        }

        public JsonObject toJson() {
            JsonObject jo = new JsonObject();
            jo.put("enable", JsonPrimitive.of(enable), "有効かどうか");
            jo.put("api_key", JsonPrimitive.of(apiKey), "APIキー");
            return jo;
        }

        @Override
        public @NotNull String getApiKey() {
            return apiKey;
        }

        @Override
        public boolean isEnable() {
            return enable;
        }
    }

    /**
     * VOICEVOXコンフィグの実装
     *
     * @param enable    有効かどうか
     * @param apiUrls   APIのURLリスト
     * @param checkTime APIが利用可能かどうか確認する間隔(ms)
     */
    private record VoicevoxConfigImpl(boolean enable, List<String> apiUrls, long checkTime) implements VoicevoxConfig {

        private VoicevoxConfigImpl() {
            this(DEFAULT_ENABLE, DEFAULT_API_URLS, DEFAULT_CHECK_TIME);
        }

        public static VoicevoxConfigImpl fromJson(JsonObject jo) {
            boolean enable = jo.getBoolean("enable", DEFAULT_ENABLE);

            List<String> loadApiUrls = Json5Utils.getStringListOfJsonArray(jo, "api_url");
            List<String> apiUrls = loadApiUrls.isEmpty() ? DEFAULT_API_URLS : loadApiUrls;

            long checkTime = jo.getLong("check_time", DEFAULT_CHECK_TIME);
            return new VoicevoxConfigImpl(enable, apiUrls, checkTime);
        }

        public JsonObject toJson() {
            JsonObject jo = new JsonObject();
            jo.put("enable", JsonPrimitive.of(enable), "有効かどうか");
            jo.put("api_url", Json5Utils.toJsonArray(this.apiUrls), "EngineのURL");
            jo.put("check_time", JsonPrimitive.of(checkTime), "APIが利用可能かどうか確認する間隔(ms)");
            return jo;
        }

        @Override
        public @NotNull @Unmodifiable List<String> getApiUrls() {
            return apiUrls;
        }

        @Override
        public long getCheckTime() {
            return checkTime;
        }

        @Override
        public boolean isEnable() {
            return enable;
        }
    }

    /**
     * データベースコンフィグの実装
     *
     * @param type         データベースの種類
     * @param host         ホスト名
     * @param port         ポート番号
     * @param databaseName データベース名
     * @param user         ユーザー名
     * @param password     パスワード
     */
    private record DataBaseConfigImpl(
            DataBaseType type,
            String host,
            @Range(from = 0, to = 65535) int port,
            String databaseName,
            String user,
            String password
    ) implements DataBaseConfig {

        private DataBaseConfigImpl() {
            this(DEFAULT_TYPE, DEFAULT_HOST, DEFAULT_PORT, DEFAULT_DATABASE_NAME, DEFAULT_USER, DEFAULT_PASSWORD);
        }

        public static DataBaseConfigImpl fromJson(JsonObject jo) {
            String typeText = Json5Utils.getStringOrElse(jo, "type", DEFAULT_TYPE.getName());
            DataBaseType type = NameSerializableEnum.getByName(DataBaseType.class, typeText, DEFAULT_TYPE);
            String host = Json5Utils.getStringOrElse(jo, "host", DEFAULT_HOST);
            int port = Json5Utils.getInt(jo, "port");
            String databaseName = Json5Utils.getStringOrElse(jo, "database_name", DEFAULT_DATABASE_NAME);
            String user = Json5Utils.getStringOrElse(jo, "user", DEFAULT_USER);
            String password = Json5Utils.getStringOrElse(jo, "password", DEFAULT_PASSWORD);

            return new DataBaseConfigImpl(type, host, port, databaseName, user, password);
        }

        public JsonObject toJson() {
            JsonObject jo = new JsonObject();
            jo.put("type", JsonPrimitive.of(type.getName()), "種類 [sqlite/mysql]");
            jo.put("host", JsonPrimitive.of(host), "ホスト名 (MySQL)");
            jo.put("port", new JsonPrimitive(port), "ポート番号 (MySQL)");
            jo.put("database_name", JsonPrimitive.of(databaseName), "データベース名 (MySQL)");
            jo.put("user", JsonPrimitive.of(user), "ユーザー名 (MySQL)");
            jo.put("password", JsonPrimitive.of(password), "パスワード (MySQL)");
            return jo;
        }

        @Override
        public @NotNull DataBaseType getType() {
            return type;
        }

        @Override
        public @NotNull String getHost() {
            return host;
        }

        @Override
        public @Range(from = 0, to = 65535) int getPort() {
            return port;
        }

        @Override
        public @NotNull String getDatabaseName() {
            return databaseName;
        }

        @Override
        public @NotNull String getUser() {
            return user;
        }

        @Override
        public @NotNull String getPassword() {
            return password;
        }
    }
}
