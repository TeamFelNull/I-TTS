package dev.felnull.ttsvoice;

import com.google.gson.JsonObject;
import dev.felnull.ttsvoice.util.DiscordUtils;
import dev.felnull.ttsvoice.util.JsonUtils;

public class ServerConfig {
    private boolean needJoin = false;
    private boolean overwriteAloud = true;
    private boolean inmMode = false;
    private boolean joinSayName = false;
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
    }

    public void save(JsonObject jo) {
        jo.addProperty("need_join", needJoin);
        jo.addProperty("overwrite_aloud", overwriteAloud);
        jo.addProperty("inm_mode", inmMode);
        jo.addProperty("join_say_name", joinSayName);
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
}
