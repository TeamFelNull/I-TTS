package dev.felnull.ttsvoice.data.dictionary;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.felnull.ttsvoice.data.SaveDataBase;
import dev.felnull.ttsvoice.util.JsonUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class SavedDictionary extends SaveDataBase implements Dictionary {
    private static final Gson GSON = new Gson();
    private static final int VERSION = 0;
    private final Map<String, String> dictEntry = new HashMap<>();

    public SavedDictionary(File saveFile, boolean loadDefault) {
        super(saveFile);
        if (loadDefault)
            loadDefault();
    }

    private void loadDefault() {
        dictEntry.put("```(.|\n)*```", "コードブロック省略");
        dictEntry.put("http(s)?:\\/\\/([\\w-]+\\.)+[\\w-]+(\\/[\\w\\- .\\/?%&=~#:,]*)?", "ユーアールエル省略");
        dictEntry.put("test", "てすと");
        dictEntry.put("土方", "どかた");
    }

    public void load(JsonObject jo, boolean loadFromFile, boolean overwrite) {
        synchronized (dictEntry) {
            if (overwrite)
                dictEntry.clear();
        }

        Integer version = JsonUtils.getInteger(jo, "version");
        if (version == null)
            throw new RuntimeException("No version");
        if (version > VERSION)
            throw new RuntimeException("Unsupported version");

        var entryEl = jo.get("entry");
        if (entryEl == null || !entryEl.isJsonObject())
            throw new RuntimeException("Invalid entry");

        var entry = entryEl.getAsJsonObject();

        Map<String, String> en = new HashMap<>();
        for (String key : entry.keySet()) {
            var je = entry.get(key);
            if (je == null || !je.isJsonPrimitive() || !je.getAsJsonPrimitive().isString())
                throw new RuntimeException("Invalid entry value");

            var str = je.getAsString();

            if (key.length() > 1000 || str.length() > 1000)
                throw new RuntimeException("Max 1000 characters");

            en.put(key, str);
        }

        synchronized (dictEntry) {
            dictEntry.putAll(en);
        }

        if (!loadFromFile) {
            saved();
        }
    }

    public String get(String word) {
        synchronized (dictEntry) {
            return dictEntry.get(word);
        }
    }

    public void add(String word, String reading) {
        synchronized (dictEntry) {
            dictEntry.put(word, reading);
        }
        saved();
    }

    public void remove(String word) {
        synchronized (dictEntry) {
            dictEntry.remove(word);
        }
        saved();
    }

    public boolean isExist(String word) {
        synchronized (dictEntry) {
            return dictEntry.containsKey(word);
        }
    }


    public void load(File file) throws IOException {
        if (file.exists()) {
            try (Reader reader = new InputStreamReader(new FileInputStream(file)); Reader bufReader = new BufferedReader(reader)) {
                var jo = GSON.fromJson(reader, JsonObject.class);
                if (jo != null)
                    load(jo, true, true);
            }
        } else {
            synchronized (dictEntry) {
                dictEntry.clear();
            }
        }
    }

    @Override
    public String replace(String text) {
        String[] tex = {text};
        synchronized (dictEntry) {
            dictEntry.forEach((k, e) -> tex[0] = tex[0].replaceAll(k, e));
        }
        return tex[0];
    }

    @Override
    public Map<String, String> getEntryShowTexts() {
        synchronized (dictEntry) {
            return ImmutableMap.copyOf(dictEntry);
        }
    }

    @Override
    public String getName() {
        return "カスタム辞書";
    }

    @Override
    public boolean isBuildIn() {
        return false;
    }

    @Override
    public JsonObject save() {
        var jo = new JsonObject();
        jo.addProperty("version", VERSION);

        var ejo = new JsonObject();

        synchronized (dictEntry) {
            dictEntry.forEach(ejo::addProperty);
        }

        jo.add("entry", ejo);
        return jo;
    }

    @Override
    public void load(JsonObject jo) {
        load(jo, true, true);
    }
}
