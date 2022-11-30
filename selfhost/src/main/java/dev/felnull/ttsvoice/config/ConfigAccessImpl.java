package dev.felnull.ttsvoice.config;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonGrammar;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.api.SyntaxError;
import dev.felnull.ttsvoice.config.json5.Json5Config;
import dev.felnull.ttsvoice.core.TTSVoiceRuntime;
import dev.felnull.ttsvoice.core.config.Config;
import dev.felnull.ttsvoice.core.config.ConfigAccess;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ConfigAccessImpl implements ConfigAccess {
    private static final Jankson JANKSON = Jankson.builder().build();
    private static final File CONFIG_FILE = new File("config.json5");

    @Override
    public @Nullable Config loadConfig(TTSVoiceRuntime runtime) {
        JsonObject jo = null;
        if (CONFIG_FILE.exists()) {
            try {
                jo = JANKSON.load(CONFIG_FILE);
            } catch (SyntaxError | IOException e) {
                runtime.getLogger().error("Failed to load config", e);
                return null;
            }
        }

        var config = new Json5Config(jo);

        if (jo == null) try (Writer writer = new BufferedWriter(new FileWriter(CONFIG_FILE, StandardCharsets.UTF_8))) {
            config.toJson().toJson(writer, JsonGrammar.JSON5, 0);
        } catch (IOException e) {
            runtime.getLogger().error("Failed to overwrite config", e);
        }

        return config;
    }

}
