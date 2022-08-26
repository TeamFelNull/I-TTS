package dev.felnull.ttsvoice.data;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.felnull.ttsvoice.tts.TTSManager;
import dev.felnull.ttsvoice.util.DiscordUtils;
import dev.felnull.ttsvoice.util.JsonUtils;
import dev.felnull.ttsvoice.voice.VoiceType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaveData extends SaveDataBase {
    private final Map<Long, String> userVoiceTypes = new HashMap<>();
    private final Map<Long, List<Long>> denyUsers = new HashMap<>();
    private final Map<Long, String> userNickNames = new HashMap<>();
    private String lastVersion = "";
    private long lastTime;

    public SaveData(File saveFile) {
        super(saveFile);
    }

    @Override
    public void load(JsonObject jo) {
        synchronized (userVoiceTypes) {
            userVoiceTypes.clear();

            if (jo.has("UserVoiceTypes")) {
                var juvt = jo.getAsJsonObject("UserVoiceTypes");

                for (Map.Entry<String, JsonElement> entry : juvt.entrySet()) {
                    userVoiceTypes.put(Long.parseLong(entry.getKey()), entry.getValue().getAsString());
                }
            }
        }

        synchronized (denyUsers) {
            denyUsers.clear();
            if (jo.has("DenyUsers")) {
                var dus = jo.getAsJsonObject("DenyUsers");

                for (Map.Entry<String, JsonElement> entry : dus.entrySet()) {
                    var ja = entry.getValue().getAsJsonArray();
                    List<Long> lst = new ArrayList<>();
                    for (JsonElement element : ja) {
                        lst.add(element.getAsLong());
                    }
                    denyUsers.put(Long.parseLong(entry.getKey()), lst);
                }
            }
        }

        synchronized (userNickNames) {
            userNickNames.clear();
            if (jo.has("UserNickNames")) {
                var unn = jo.getAsJsonObject("UserNickNames");
                for (Map.Entry<String, JsonElement> entry : unn.entrySet()) {
                    userNickNames.put(Long.parseLong(entry.getKey()), entry.getValue().getAsString());
                }
            }
        }

        lastVersion = JsonUtils.getString(jo, "LastVersion");

        var lstTime = JsonUtils.getLong(jo, "LastTime");
        if (lstTime != null)
            lastTime = lstTime;
    }

    public VoiceType getVoiceType(long userId, long guildId) {
        synchronized (userVoiceTypes) {
            var vt = userVoiceTypes.get(userId);
            if (vt != null)
                return TTSManager.getInstance().getVoiceTypeById(vt, userId, guildId);
        }
        return null;
    }

    public void setVoiceType(long userId, VoiceType voiceType) {
        synchronized (userVoiceTypes) {
            userVoiceTypes.put(userId, voiceType.getId());
        }
        saved();
    }

    public List<Long> getDenyUsers(long guildId) {
        synchronized (denyUsers) {
            return ImmutableList.copyOf(denyUsers.computeIfAbsent(guildId, n -> new ArrayList<>()));
        }
    }

    public boolean isDenyUser(long guildId, long userId) {
        synchronized (denyUsers) {
            return denyUsers.computeIfAbsent(guildId, n -> new ArrayList<>()).contains(userId);
        }
    }

    public void addDenyUser(long guildId, long userId) {
        synchronized (denyUsers) {
            denyUsers.computeIfAbsent(guildId, n -> new ArrayList<>()).add(userId);
        }
        saved();
    }

    public void removeDenyUser(long guildId, long userId) {
        synchronized (denyUsers) {
            denyUsers.computeIfAbsent(guildId, n -> new ArrayList<>()).remove(userId);
        }
        saved();
    }

    public void setUserNickName(long userId, String name) {
        synchronized (userNickNames) {
            userNickNames.put(userId, name);
        }
        saved();
    }

    public void removeUserNickName(long userId) {
        synchronized (userNickNames) {
            userNickNames.remove(userId);
        }
        saved();
    }

    public String getUserNickName(long userId) {
        synchronized (userNickNames) {
            return DiscordUtils.mentionEscape(userNickNames.get(userId));
        }
    }

    public void setLastVersion(String lastVersion) {
        this.lastVersion = lastVersion;
        saved();
    }

    public String getLastVersion() {
        return lastVersion;
    }

    public void setLastTime(long time) {
        this.lastTime = time;
        saved();
    }

    public long getLastTime() {
        return lastTime;
    }

    @Override
    public JsonObject save() {
        var jo = new JsonObject();
        var juvt = new JsonObject();
        synchronized (userVoiceTypes) {
            userVoiceTypes.forEach((n, m) -> juvt.addProperty(n.toString(), m));
        }
        jo.add("UserVoiceTypes", juvt);

        var jdu = new JsonObject();
        synchronized (denyUsers) {
            denyUsers.forEach((n, m) -> {
                var ja = new JsonArray();
                m.forEach(ja::add);
                jdu.add(n.toString(), ja);
            });
        }
        jo.add("DenyUsers", jdu);

        var unn = new JsonObject();
        synchronized (userNickNames) {
            userNickNames.forEach((n, m) -> unn.addProperty(n.toString(), m));
        }
        jo.add("UserNickNames", unn);

        jo.addProperty("LastVersion", lastVersion);
        jo.addProperty("LastTime", lastTime);

        return jo;
    }
}
