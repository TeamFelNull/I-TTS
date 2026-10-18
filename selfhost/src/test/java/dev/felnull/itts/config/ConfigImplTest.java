package dev.felnull.itts.config;

import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import dev.felnull.itts.config.old.ConfigV1;
import dev.felnull.itts.core.config.DataBaseConfig;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * コンフィグの省略項目と移行を検証する
 */
class ConfigImplTest {
    @Test
    void keepsOpenAiTtsDisabledForExistingAndMigratedConfigs() {
        assertFalse(ConfigImpl.LOADER.load(new JsonObject()).getOpenAiTtsConfig().isEnable());
        assertFalse(ConfigImpl.createInitialConfig().getOpenAiTtsConfig().isEnable());
        assertFalse(ConfigImpl.LOADER.migrate(ConfigV1.LOADER.load(new JsonObject())).getOpenAiTtsConfig().isEnable());
        ConfigImpl version2 = ConfigImpl.LOADER.load(new JsonObject());
        assertEquals(version2, ConfigImpl.LOADER.migrate(version2));
    }

    @Test
    void roundTripsOpenAiTtsSettings() {
        JsonObject json = new JsonObject();
        OpenAiTtsConfigImpl tts = new OpenAiTtsConfigImpl(true, "http://localhost:8000/v1/", "test-key", "custom-model", List.of("speaker-a", "speaker-b"));
        json.put("openai_tts", tts.toJson());
        ConfigImpl config = ConfigImpl.LOADER.load(json);
        assertEquals(tts, config.getOpenAiTtsConfig());
        JsonObject saved = new JsonObject();
        config.writeToJson(saved);
        assertEquals(tts, ConfigImpl.LOADER.load(saved).getOpenAiTtsConfig());
    }

    @Test
    void rejectsInvalidEnabledOpenAiTtsSettings() {
        assertThrows(IllegalArgumentException.class, () -> new OpenAiTtsConfigImpl(true, "file:///tmp/audio", "", "model", List.of("speaker")));
        assertThrows(IllegalArgumentException.class, () -> new OpenAiTtsConfigImpl(true, "http://localhost/v1?query=1", "", "model", List.of("speaker")));
        assertThrows(IllegalArgumentException.class, () -> new OpenAiTtsConfigImpl(true, "http://localhost/v1", "", "", List.of("speaker")));
        assertThrows(IllegalArgumentException.class, () -> new OpenAiTtsConfigImpl(true, "http://localhost/v1", "", "model", List.of()));
        assertThrows(IllegalArgumentException.class, () -> new OpenAiTtsConfigImpl(true, "http://localhost/v1", "", "model", List.of(" ")));
    }

    @Test
    void loadsOmittedDatabaseAndStatistics() {
        ConfigImpl config = ConfigImpl.LOADER.load(new JsonObject());
        assertEquals(DataBaseConfig.DEFAULT_PORT, config.getDataBaseConfig().getPort());
        assertEquals(DataBaseConfig.DEFAULT_PORT, config.getStatisticsConfig().getDataBase().getPort());
    }

    @Test
    void loadsDisabledStatisticsWithoutDatabase() {
        JsonObject json = new JsonObject();
        JsonObject statistics = new JsonObject();
        statistics.put("enable", JsonPrimitive.of(false));
        json.put("statistics", statistics);
        assertFalse(ConfigImpl.LOADER.load(json).getStatisticsConfig().isEnable());
    }

    @Test
    void preservesExplicitPortAndRejectsInvalidPort() {
        JsonObject json = new JsonObject();
        JsonObject database = new JsonObject();
        json.put("data_base", database);
        database.put("port", new JsonPrimitive(3307));
        assertEquals(3307, ConfigImpl.LOADER.load(json).getDataBaseConfig().getPort());
        database.put("port", JsonPrimitive.of("invalid"));
        assertThrows(IllegalStateException.class, () -> ConfigImpl.LOADER.load(json));
    }

    @Test
    void migratesV1WithoutDatabasePort() {
        ConfigImpl config = ConfigImpl.LOADER.migrate(ConfigV1.LOADER.load(new JsonObject()));
        assertEquals(DataBaseConfig.DEFAULT_PORT, config.getDataBaseConfig().getPort());
    }
}
