package dev.felnull.itts.config;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonGrammar;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import blue.endless.jankson.api.SyntaxError;
import com.google.common.collect.ImmutableMap;
import dev.felnull.fnjl.util.FNDataUtil;
import dev.felnull.itts.config.old.ConfigV0;
import dev.felnull.itts.core.config.Config;
import dev.felnull.itts.core.config.ConfigContext;
import dev.felnull.itts.utils.Json5Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * SelfHost用コンフィグマネージャー
 */
public class SelfHostConfigManager implements ConfigContext {

    /**
     * インスタンス
     */
    private static final SelfHostConfigManager INSTANCE = new SelfHostConfigManager();

    /**
     * Jankson
     */
    private static final Jankson JANKSON = Jankson.builder().build();

    /**
     * ロガー
     */
    private static final Logger LOGGER = LogManager.getLogger(SelfHostConfigManager.class);

    /**
     * コンフィグファイル
     */
    private static final File CONFIG_FILE = new File("./config.json5");

    /**
     * 旧コンフィグフォルダ
     */
    private static final File OLD_CONFIG_FOLDER = new File("./old_config");

    /**
     * コンフィグローダー
     */
    private final Map<Integer, ConfigLoader<?>> configLoaders = ImmutableMap.of(
            0, ConfigV0.LOADER,
            1, ConfigImpl.LOADER
    );

    /**
     * インスタンス取得
     *
     * @return シングルトンインスタンス
     */
    public static SelfHostConfigManager getInstance() {
        return INSTANCE;
    }

    @Override
    public @Nullable Config loadConfig() {
        LOGGER.info("Config load start");

        int latestVersionNum = configLoaders.keySet().stream()
                .max(Integer::compareTo)
                .orElseThrow();

        // コンフィグファイルが存在しない場合
        if (!CONFIG_FILE.exists()) {
            writeConfig(ConfigImpl.createInitialConfig(), latestVersionNum);
            throw new IllegalStateException("Generate config file because it does not exist.");
        }

        // コンフィグ読み込み
        JsonObject configJo;
        try {
            configJo = JANKSON.load(CONFIG_FILE);
        } catch (IOException | SyntaxError e) {
            throw new IllegalStateException("Failed to load config", e);
        }
        int configVersionNum = Json5Utils.getInt(configJo, "config_version");

        Config config;

        if (configVersionNum == latestVersionNum) {
            // 現在のバージョンと同じ場合は普通に読み込む
            config = ConfigImpl.LOADER.load(configJo);
        } else if (configVersionNum < latestVersionNum) {
            // バージョンが古い場合は移行して読み込む
            LOGGER.info("Migrate config: {} → {}", configVersionNum, latestVersionNum);

            Object migrateConfig = null;
            for (int i = configVersionNum; i <= latestVersionNum; i++) {
                ConfigLoader<?> loader = configLoaders.get(i);
                if (migrateConfig == null) {
                    migrateConfig = loader.load(configJo);
                } else {
                    migrateConfig = loader.migrate(migrateConfig);
                }
            }

            ConfigImpl newConfig = (ConfigImpl) migrateConfig;

            // 古いコンフィグを旧コンフィグフォルダへコピー
            FNDataUtil.wishMkdir(OLD_CONFIG_FOLDER);
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss");
            String timeText = LocalDateTime.now().format(timeFormatter);
            File oldConfigFIle = new File(OLD_CONFIG_FOLDER, "config_v" + configVersionNum + "_" + timeText + ".json5");
            try {
                Files.copy(CONFIG_FILE.toPath(), oldConfigFIle.toPath());
            } catch (IOException e) {
                throw new IllegalStateException("Copy failed", e);
            }

            // コンフィグ上書き
            writeConfig(newConfig, latestVersionNum);

            config = newConfig;
        } else {
            // バージョンが新しい場合はエラー
            throw new IllegalStateException("Unsupported config version: " + configVersionNum);
        }

        LOGGER.info("Config load done");

        return config;
    }

    private void writeConfig(ConfigImpl config, int version) {
        JsonObject jo = new JsonObject();
        jo.put("config_version", new JsonPrimitive(version), "コンフィグのバージョン 変更しないでください！");
        config.writeToJson(jo);

        try (BufferedWriter writer = new BufferedWriter((new FileWriter(CONFIG_FILE)))) {
            jo.toJson(writer, JsonGrammar.JSON5, 0);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write config", e);
        }
    }
}
