package dev.felnull.itts.config;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonGrammar;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import blue.endless.jankson.api.SyntaxError;
import com.google.common.collect.ImmutableMap;
import dev.felnull.itts.config.old.ConfigV0;
import dev.felnull.itts.config.old.ConfigV1;
import dev.felnull.itts.core.config.Config;
import dev.felnull.itts.core.config.ConfigContext;
import dev.felnull.itts.utils.Json5Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

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
    private final File configFile;

    /**
     * 旧コンフィグフォルダ
     */
    private final File oldConfigFolder;

    /**
     * コンフィグローダー
     */
    private final Map<Integer, ConfigLoader<?>> configLoaders = ImmutableMap.of(
            0, ConfigV0.LOADER,
            1, ConfigV1.LOADER,
            2, ConfigImpl.LOADER,
            3, ConfigImpl.LOADER
    );

    private SelfHostConfigManager() {
        this(Path.of("."));
    }

    /**
     * 設定ディレクトリを指定して初期化する
     *
     * @param directory 設定ディレクトリ
     */
    SelfHostConfigManager(Path directory) {
        this.configFile = directory.resolve("config.json5").toFile();
        this.oldConfigFolder = directory.resolve("old_config").toFile();
    }

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
        if (!configFile.exists()) {
            writeConfig(ConfigImpl.createInitialConfig(), latestVersionNum);
            throw new IllegalStateException("Generate config file because it does not exist.");
        }

        // コンフィグ読み込み
        JsonObject configJo;
        try {
            configJo = JANKSON.load(configFile);
        } catch (IOException | SyntaxError e) {
            throw new IllegalStateException("Failed to load config", e);
        }
        int configVersionNum = Json5Utils.getInt(configJo, "config_version");
        if (!configLoaders.containsKey(configVersionNum)) {
            throw new IllegalStateException("Unsupported config version: " + configVersionNum);
        }

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
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss");
            String timeText = LocalDateTime.now().format(timeFormatter);
            File oldConfigFIle = new File(oldConfigFolder, "config_v" + configVersionNum + "_" + timeText + "_" + UUID.randomUUID() + ".json5");
            try {
                Files.createDirectories(oldConfigFolder.toPath());
                Files.copy(configFile.toPath(), oldConfigFIle.toPath());
            } catch (IOException e) {
                throw new IllegalStateException("Copy failed", e);
            }

            // コンフィグ上書き
            if (configVersionNum == 2) {
                migrateV2Config(configFile.toPath(), configJo);
            } else {
                writeConfig(newConfig, latestVersionNum);
            }

            config = newConfig;
        } else {
            // バージョンが新しい場合はエラー
            throw new IllegalStateException("Unsupported config version: " + configVersionNum);
        }

        LOGGER.info("Config load done");

        return config;
    }

    /**
     * バージョン2の設定にOpenAI互換TTSの初期設定を追加し、バージョン3へ移行する
     *
     * @param configFile 設定ファイル
     * @param configJo コメントを含む既存の設定
     * @return 設定を移行した場合はtrue
     */
    static boolean migrateV2Config(Path configFile, JsonObject configJo) {
        if (Json5Utils.getInt(configJo, "config_version") != 2) {
            return false;
        }

        if (!configJo.containsKey("openai_tts")) {
            configJo.put("openai_tts", new OpenAiTtsConfigImpl().toJson(), "OpenAI互換TTSのコンフィグ");
        }
        configJo.put("config_version", new JsonPrimitive(3));
        writeJsonConfig(configFile, configJo);
        return true;
    }

    /**
     * 一時ファイルを経由して設定を保存する
     *
     * @param configFile 設定ファイル
     * @param configJo 保存する設定
     */
    static void writeJsonConfig(Path configFile, JsonObject configJo) {
        try {
            Path target = Files.exists(configFile) ? configFile.toRealPath() : configFile.toAbsolutePath();
            Path temporary = Files.createTempFile(target.getParent(), "itts-config-", ".tmp");
            try {
                try (BufferedWriter writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                    configJo.toJson(writer, JsonGrammar.JSON5, 0);
                }
                try {
                    Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (AtomicMoveNotSupportedException e) {
                    Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
                }
            } finally {
                Files.deleteIfExists(temporary);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write config", e);
        }
    }

    private void writeConfig(ConfigImpl config, int version) {
        JsonObject jo = new JsonObject();
        jo.put("config_version", new JsonPrimitive(version), "コンフィグのバージョン 変更しないでください！");
        config.writeToJson(jo);

        writeJsonConfig(configFile.toPath(), jo);
    }
}
