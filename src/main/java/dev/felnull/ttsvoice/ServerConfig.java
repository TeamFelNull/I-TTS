package dev.felnull.ttsvoice;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.felnull.ttsvoice.util.DiscordUtils;
import dev.felnull.ttsvoice.util.JsonUtils;

import java.util.HashMap;
import java.util.Map;

public class ServerConfig {
    private boolean needJoin = false;
    private boolean overwriteAloud = true;
    private boolean inmMode = false;
    private boolean joinSayName = false;
    private int maxReadAroundCharacterLimit = 200;
    private String nonReadingPrefix = ";";
    private final Map<Long, TTSEntry> lastJoinChannels = new HashMap<>();
    private boolean dirty;

    public void load(JsonObject jo) {
        var nj = JsonUtils.getBoolean(jo, "need_join");
        if (nj != null)
            needJoin = nj;

        var oa = JsonUtils.getBoolean(jo, "overwrite_aloud");
        if (oa != null)
            overwriteAloud = oa;

        var im = JsonUtils.getBoolean(jo, "inm_mode");
        if (im != null)
            inmMode = im;

        var jsn = JsonUtils.getBoolean(jo, "join_say_name");
        if (jsn != null)
            joinSayName = jsn;

        var mracl = JsonUtils.getInteger(jo, "max_read_around_character_limit");
        if (mracl != null)
            maxReadAroundCharacterLimit = mracl;

        var nrp = JsonUtils.getString(jo, "non-reading_prefix");
        if (nrp != null)
            nonReadingPrefix = nrp;


        if (jo.has("last_join") && jo.get("last_join").isJsonObject()) {
            var joe = jo.getAsJsonObject("last_join");
            for (Map.Entry<String, JsonElement> entry : joe.entrySet()) {
                lastJoinChannels.put(Long.parseLong(entry.getKey()), TTSEntry.of(entry.getValue().getAsJsonObject()));
            }
        }

    }

    public void save(JsonObject jo) {
        jo.addProperty("need_join", needJoin);
        jo.addProperty("overwrite_aloud", overwriteAloud);
        jo.addProperty("inm_mode", inmMode);
        jo.addProperty("join_say_name", joinSayName);
        jo.addProperty("max_read_around_character_limit", maxReadAroundCharacterLimit);
        jo.addProperty("non-reading_prefix", nonReadingPrefix);

        var ljjo = new JsonObject();
        for (Map.Entry<Long, TTSEntry> entry : lastJoinChannels.entrySet()) {
            ljjo.add(String.valueOf(entry.getKey()), entry.getValue().toJson());
        }
        jo.add("last_join", ljjo);
    }

    public boolean isInmMode(long guildId) {
        if (DiscordUtils.isNonAllowInm(guildId))
            return false;
        return inmMode;
    }

    public boolean isOverwriteAloud() {
        return overwriteAloud;
    }

    public boolean isNeedJoin() {
        return needJoin;
    }

    public boolean isJoinSayName() {
        return joinSayName;
    }

    public int getMaxReadAroundCharacterLimit() {
        return maxReadAroundCharacterLimit;
    }

    public String getNonReadingPrefix() {return nonReadingPrefix;}

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public void setInmMode(boolean inmMode) {
        this.inmMode = inmMode;
        dirty = true;
    }

    public void setNeedJoin(boolean needJoin) {
        this.needJoin = needJoin;
        dirty = true;
    }

    public void setOverwriteAloud(boolean overwriteAloud) {
        this.overwriteAloud = overwriteAloud;
        dirty = true;
    }

    public void setJoinSayName(boolean joinSayName) {
        this.joinSayName = joinSayName;
        dirty = true;
    }

    public void setMaxReadAroundCharacterLimit(int maxReadAroundCharacterLimit) {
        this.maxReadAroundCharacterLimit = maxReadAroundCharacterLimit;
        dirty = true;
    }

    public void setNonReadingPrefix(String NonReadingPrefix) {
        this.nonReadingPrefix = NonReadingPrefix;
        dirty = true;
    }

    public void setLastJoinChannel(long botUserId, TTSEntry ttsEntry) {
        lastJoinChannels.put(botUserId, ttsEntry);
        dirty = true;
    }

    public void removeLastJoinChannel(long botUserId) {
        lastJoinChannels.remove(botUserId);
        dirty = true;
    }

    public TTSEntry getLastJoinChannel(long botUserId) {
        return lastJoinChannels.get(botUserId);
    }

    public static record TTSEntry(long audioChannel, long ttsChannel) {
        public JsonObject toJson() {
            var jo = new JsonObject();
            jo.addProperty("audio_channel", audioChannel);
            jo.addProperty("tts_channel", ttsChannel);
            return jo;
        }

        public static TTSEntry of(JsonObject jo) {
            return new TTSEntry(jo.get("audio_channel").getAsLong(), jo.get("tts_channel").getAsLong());
        }
    }
}
