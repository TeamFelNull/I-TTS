package dev.felnull.ttsvoice.config;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonGrammar;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.api.SyntaxError;
import dev.felnull.ttsvoice.Main;
import dev.felnull.ttsvoice.core.config.Config;
import dev.felnull.ttsvoice.core.config.ConfigAccess;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class SelfHostConfigManager implements ConfigAccess {
    private static final SelfHostConfigManager INSTANCE = new SelfHostConfigManager();
    private static final Jankson JANKSON = Jankson.builder().build();
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
                Main.RUNTIME.getLogger().error("Failed to load config", e);
                return null;
            }
        }

        if (jo != null) {
            int version = jo.getInt("config_version", -1);
            if (version != Config.VERSION) {
                Main.RUNTIME.getLogger().error("Unsupported config version");
                return null;
            }
        }

        var config = new ConfigImpl(Optional.ofNullable(jo).orElseGet(JsonObject::new));

        if (jo == null) try (Writer writer = new BufferedWriter(new FileWriter(CONFIG_FILE, StandardCharsets.UTF_8))) {
            config.toJson().toJson(writer, JsonGrammar.JSON5, 0);
        } catch (IOException e) {
            Main.RUNTIME.getLogger().error("Failed to overwrite config", e);
        }

        return config;
    }
}
