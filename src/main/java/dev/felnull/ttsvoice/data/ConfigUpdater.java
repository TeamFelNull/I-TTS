package dev.felnull.ttsvoice.data;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonGrammar;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import com.google.common.collect.ImmutableList;
import dev.felnull.fnjl.util.FNDataUtil;
import dev.felnull.ttsvoice.util.Json5Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Files;

public class ConfigUpdater {
    private static final File OLD_DATA_FOLDER = new File("./old");
    private static final Jankson JANKSON = Jankson.builder().build();

    public void run(File configFile) throws Exception {
        if (!configFile.exists()) return;

        var jo = JANKSON.load(configFile);
        int cfgv = jo.getInt("ConfigVersion", 0);

        if (cfgv == 1 || cfgv == 0) {
            ConfigAndSaveDataManager.LOGGER.info("update the config: " + cfgv + "->2");
            var fol = new File(OLD_DATA_FOLDER, "config");
            FNDataUtil.wishMkdir(fol);
            String fname = configFile.getName() + ".bk0";
            int ct = 1;
            while (fol.toPath().resolve(fname).toFile().exists()) {
                fname = fname.substring(0, fname.length() - 1) + (ct++);
            }
            Files.copy(configFile.toPath(), fol.toPath().resolve(fname));
            port1To2(jo);

            try (Writer writer = new BufferedWriter(new FileWriter(configFile))) {
                jo.toJson(writer, JsonGrammar.JSON5, 0);
            }
        }
    }

    private void port1To2(JsonObject jo) {
        jo.put("ConfigVersion", new JsonPrimitive(2), "コンフィグバージョン(変更しないでください)");
        jo.put("ShareVoxURL", Json5Utils.toJsonArray(ImmutableList.of("http://127.0.0.1:50025")), "ShareVoxのURL指定");

        var cjo = jo.getObject("VoiceConfig");
        if (cjo != null)
            cjo.put("ShareVox", JsonPrimitive.of(true));

    }

}
