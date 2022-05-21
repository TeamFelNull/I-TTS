package dev.felnull.ttsvoice;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.felnull.ttsvoice.tts.IVoiceType;
import dev.felnull.ttsvoice.tts.TTSManager;

import java.util.HashMap;
import java.util.Map;

public class SaveData {
    private final Map<Long, String> userVoiceTypes = new HashMap<>();
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
    }

    public JsonObject save() {
        var jo = new JsonObject();
        var juvt = new JsonObject();
        synchronized (userVoiceTypes) {
            userVoiceTypes.forEach((n, m) -> juvt.addProperty(n.toString(), m));
        }
        jo.add("UserVoiceTypes", juvt);
        return jo;
    }

    public IVoiceType getVoiceType(long userId) {
        synchronized (userVoiceTypes) {
            var vt = userVoiceTypes.get(userId);
            if (vt != null)
                return TTSManager.getInstance().getVoiceTypeById(vt);
        }
        return null;
    }

    public void setVoiceType(long userId, IVoiceType voiceType) {
        synchronized (userVoiceTypes) {
            userVoiceTypes.put(userId, voiceType.getId());
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
