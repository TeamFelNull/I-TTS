package dev.felnull.ttsvoice.core.tts;

import dev.felnull.ttsvoice.core.TTSVoiceRuntime;
import dev.felnull.ttsvoice.core.tts.saidtext.SaidText;
import dev.felnull.ttsvoice.core.voice.Voice;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TTSManager {
    private final Map<Long, TTSInstance> instances = new ConcurrentHashMap<>();

    public void setReadAroundChannel(@NotNull Guild guild, @NotNull MessageChannel textChannel) {
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
        var serverData = TTSVoiceRuntime.getInstance().getSaveDataManager().getServerData(guildId);
        instances.put(guildId, new TTSInstance(guild, channelId, data.getReadAroundTextChannel(), serverData.isOverwriteAloud()));
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

    public void reload(@NotNull Guild guild) {
        long guildId = guild.getIdLong();

        var instance = getTTSInstance(guildId);
        if (instance == null)
            return;

        disconnect(guild);

        var rc = guild.getChannelById(AudioChannel.class, instance.getAudioChannel());
        if (rc != null)
            connect(guild, rc);
    }

    @Nullable
    public TTSInstance getTTSInstance(long guildId) {
        return instances.get(guildId);
    }

    public void sayChat(@NotNull Guild guild, @NotNull MessageChannel messageChannel, @NotNull User user, @Nullable Member member, @NotNull String message) {
        long guildId = guild.getIdLong();
        long textChannelId = messageChannel.getIdLong();
        long userId = user.getIdLong();

        if (member == null) return;

        var ti = getTTSInstance(guildId);
        if (ti == null || ti.getTextChannel() != textChannelId) return;

        var sm = TTSVoiceRuntime.getInstance().getSaveDataManager();

        if (sm.getServerData(guildId).isNeedJoin()) {
            var vs = member.getVoiceState();
            if (vs == null) return;

            var vc = vs.getChannel();
            if (vc == null || vc.getIdLong() != ti.getAudioChannel()) return;
        }

        var vt = TTSVoiceRuntime.getInstance().getVoiceManager().getVoiceType(guildId, userId);
        if (vt == null) return;

        ti.sayText(SaidText.literal(Voice.simple(vt), message));
    }
}
