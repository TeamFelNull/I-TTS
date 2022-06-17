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
    private final Map<Long, String> userNickNames = new HashMap<>();
    private boolean dirty;

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

        var unn = new JsonObject();
        synchronized (userNickNames) {
            userNickNames.forEach((n, m) -> unn.addProperty(n.toString(), m));
        }
        jo.add("UserNickNames", unn);

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

    public void setUserNickName(long userId, String name) {
        synchronized (userNickNames) {
            userNickNames.put(userId, name);
        }
        dirty = true;
    }

    public void removeUserNickName(long userId) {
        synchronized (userNickNames) {
            userNickNames.remove(userId);
        }
        dirty = true;
    }

    public String getUserNickName(long userId) {
        synchronized (userNickNames) {
            return userNickNames.get(userId);
        }
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean isDirty() {
        return dirty;
    }
}
