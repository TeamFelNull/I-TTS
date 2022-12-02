package dev.felnull.ttsvoice.core.tts;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class TTSManager {
    private final Map<Long, TTSInstance> instances = new HashMap<>();

    public void connect(long guildId, long channel) {
        var pre = getTTSInstance(guildId);

        if (pre != null) {
            if (pre.getChannel() == channel)
                return;
            disconnect(guildId);
        }

        instances.put(guildId, new TTSInstance(channel));
    }

    public void disconnect(long guildId) {
        var instance = getTTSInstance(guildId);
        if (instance == null)
            return;

        instance.destroy();

        instances.remove(guildId);
    }

    @Nullable
    public TTSInstance getTTSInstance(long guildId) {
        return instances.get(guildId);
    }
}
