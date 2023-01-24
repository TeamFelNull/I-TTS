package dev.felnull.ttsvoice.core.tts;

import dev.felnull.ttsvoice.core.TTSVoiceRuntime;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TTSManager {
    private final Map<Long, TTSInstance> instances = new ConcurrentHashMap<>();
    private final TTSVoiceRuntime runtime;

    public TTSManager(TTSVoiceRuntime runtime) {
        this.runtime = runtime;
    }

    public void setReadAroundChannel(Guild guild, long textChannel) {
        long guildId = guild.getIdLong();
        var data = runtime.getSaveDataManager().getBotStateData(guildId);
        data.setReadAroundTextChannel(textChannel);
    }

    public void connect(Guild guild, long audioChannel) {
        long guildId = guild.getIdLong();
        var pre = getTTSInstance(guildId);

        if (pre != null) {
            if (pre.getAudioChannel() == audioChannel)
                return;
            disconnect(guild);
        }

        var data = runtime.getSaveDataManager().getBotStateData(guildId);
        instances.put(guildId, new TTSInstance(runtime, guild, audioChannel, data.getReadAroundTextChannel()));
        data.setConnectedAudioChannel(audioChannel);
    }

    public void disconnect(Guild guild) {
        long guildId = guild.getIdLong();

        var instance = getTTSInstance(guildId);
        if (instance == null)
            return;

        instance.dispose();
        instances.remove(guildId);

        var data = runtime.getSaveDataManager().getBotStateData(guildId);
        data.setConnectedAudioChannel(-1);
    }

    @Nullable
    public TTSInstance getTTSInstance(long guildId) {
        return instances.get(guildId);
    }

    public void receivedChat(long guildId, long channelId, String message) {
        var ti = getTTSInstance(guildId);
        if (ti == null) return;
        if (ti.getTextChannel() != channelId) return;

        ti.sayText(message);
    }
}
