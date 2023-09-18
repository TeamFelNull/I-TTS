package dev.felnull.itts.config;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonGrammar;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.api.SyntaxError;
import dev.felnull.itts.Main;
import dev.felnull.itts.core.config.Config;
import dev.felnull.itts.core.config.ConfigContext;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * セルフホストのコンフィグ管理
 *
 * @author MORIMORI0317
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
     * コンフィグファイル
     */
    private static final File CONFIG_FILE = new File("config.json5");

    public static SelfHostConfigManager getInstance() {
        return INSTANCE;
    }

    @Override
    public @Nullable Config loadConfig() {
        JsonObject jo = null;
        if (CONFIG_FILE.exists()) {
            try {
                jo = JANKSON.load(CONFIG_FILE);
            } catch (SyntaxError | IOException e) {
                Main.runtime.getLogger().error("Failed to load config", e);
                return null;
            }
        }

        if (jo != null) {
            int version = jo.getInt("config_version", -1);
            if (version != Config.VERSION) {
                Main.runtime.getLogger().error("Unsupported config version");
                return null;
            }
        }

        ConfigImpl config = new ConfigImpl(Optional.ofNullable(jo).orElseGet(JsonObject::new));

        if (jo == null) {
            try (Writer writer = new BufferedWriter(new FileWriter(CONFIG_FILE, StandardCharsets.UTF_8))) {
                config.toJson().toJson(writer, JsonGrammar.JSON5, 0);
            } catch (IOException e) {
                Main.runtime.getLogger().error("Failed to overwrite config", e);
            }
        }

        return config;
    }
}
