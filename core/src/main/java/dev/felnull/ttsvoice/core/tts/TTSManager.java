package dev.felnull.ttsvoice.core.tts;

import dev.felnull.ttsvoice.core.TTSVoiceRuntime;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TTSManager {
    private final Map<Long, TTSInstance> instances = new ConcurrentHashMap<>();


    public void setReadAroundChannel(@NotNull Guild guild, MessageChannel textChannel) {
        long guildId = guild.getIdLong();
        var data = TTSVoiceRuntime.getInstance().getSaveDataManager().getBotStateData(guildId);
        data.setReadAroundTextChannel(textChannel.getIdLong());
    }

    public void connect(@NotNull Guild guild, @NotNull AudioChannel audioChannel) {
        long guildId = guild.getIdLong();
        long channelId = audioChannel.getIdLong();

        var pre = getTTSInstance(guildId);

        if (pre != null) {
            if (pre.getAudioChannel() == channelId)
                return;
            disconnect(guild);
        }

        var data = TTSVoiceRuntime.getInstance().getSaveDataManager().getBotStateData(guildId);
        instances.put(guildId, new TTSInstance(guild, channelId, data.getReadAroundTextChannel()));
        data.setConnectedAudioChannel(channelId);
    }

    public void disconnect(@NotNull Guild guild) {
        long guildId = guild.getIdLong();

        var instance = getTTSInstance(guildId);
        if (instance == null)
            return;

        instance.dispose();
        instances.remove(guildId);

        var data = TTSVoiceRuntime.getInstance().getSaveDataManager().getBotStateData(guildId);
        data.setConnectedAudioChannel(-1);
    }

    @Nullable
    public TTSInstance getTTSInstance(long guildId) {
        return instances.get(guildId);
    }
}
