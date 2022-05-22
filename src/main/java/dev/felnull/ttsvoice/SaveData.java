package dev.felnull.ttsvoice;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.felnull.ttsvoice.tts.IVoiceType;
import dev.felnull.ttsvoice.tts.TTSManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaveData {
    private final Map<Long, String> userVoiceTypes = new HashMap<>();
    private final Map<Long, List<Long>> denyUsers = new HashMap<>();
    private boolean dirty;

    public void load(JsonObject jo) {
        userVoiceTypes.clear();

        if (jo.has("UserVoiceTypes")) {
            var juvt = jo.getAsJsonObject("UserVoiceTypes");
            synchronized (userVoiceTypes) {
                for (Map.Entry<String, JsonElement> entry : juvt.entrySet()) {
                    userVoiceTypes.put(Long.parseLong(entry.getKey()), entry.getValue().getAsString());
                }
            }
        }

        denyUsers.clear();

        if (jo.has("DenyUsers")) {
            var dus = jo.getAsJsonObject("DenyUsers");
            synchronized (denyUsers) {
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
    }

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
        return jo;
    }

    public IVoiceType getVoiceType(long userId, long guildId) {
        synchronized (userVoiceTypes) {
            var vt = userVoiceTypes.get(userId);
            if (vt != null)
                return TTSManager.getInstance().getVoiceTypeById(vt, userId, guildId);
        }
        return null;
    }

    public void setVoiceType(long userId, IVoiceType voiceType) {
        synchronized (userVoiceTypes) {
            userVoiceTypes.put(userId, voiceType.getId());
        }
        dirty = true;
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
        dirty = true;
    }

    public void removeDenyUser(long guildId, long userId) {
        synchronized (denyUsers) {
            denyUsers.computeIfAbsent(guildId, n -> new ArrayList<>()).remove(userId);
        }
        dirty = true;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean isDirty() {
        return dirty;
    }
}
