package dev.felnull.itts.core.tts;

import dev.felnull.itts.core.ITTSRuntime;
import dev.felnull.itts.core.tts.saidtext.SaidText;
import dev.felnull.itts.core.voice.Voice;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TTSManager {
    private final Map<Long, TTSInstance> instances = new ConcurrentHashMap<>();

    public int getTTSCount() {
        return instances.size();
    }

    public void setReadAroundChannel(@NotNull Guild guild, @NotNull MessageChannel textChannel) {
        long guildId = guild.getIdLong();
        var data = ITTSRuntime.getInstance().getSaveDataManager().getBotStateData(guildId);
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

        var data = ITTSRuntime.getInstance().getSaveDataManager().getBotStateData(guildId);
        var serverData = ITTSRuntime.getInstance().getSaveDataManager().getServerData(guildId);
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

        var data = ITTSRuntime.getInstance().getSaveDataManager().getBotStateData(guildId);
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
        if (user.isBot() || user.isSystem()) return;

        var ti = getTTSInstance(guildId);
        if (ti == null || ti.getTextChannel() != textChannelId) return;

        var sm = ITTSRuntime.getInstance().getSaveDataManager();

        if (sm.getServerData(guildId).isNeedJoin()) {
            var vs = member.getVoiceState();
            if (vs == null) return;

            var vc = vs.getChannel();
            if (vc == null || vc.getIdLong() != ti.getAudioChannel()) return;
        }

        var vt = ITTSRuntime.getInstance().getVoiceManager().getVoiceType(guildId, userId);
        if (vt == null) return;

        ti.sayText(SaidText.literal(vt.createVoice(guildId, userId), message));
    }

    public void onVCEvent(@NotNull Guild guild, @NotNull Member member, @Nullable AudioChannelUnion join, @Nullable AudioChannelUnion left) {
        long guildId = guild.getIdLong();
        var user = member.getUser();
        long userId = user.getIdLong();

        var ti = getTTSInstance(guildId);
        if (ti == null || !((join != null && ti.getAudioChannel() == join.getIdLong()) || (left != null && ti.getAudioChannel() == left.getIdLong())))
            return;

        var sm = ITTSRuntime.getInstance().getSaveDataManager();
        if (!sm.getServerData(guildId).isNotifyMove()) return;

        var vt = ITTSRuntime.getInstance().getVoiceManager().getVoiceType(guildId, userId);
        if (vt == null) return;

        if (join != null && join.getIdLong() == ti.getAudioChannel()) {
            var vcs = member.getVoiceState();
            if (vcs != null && vcs.isGuildMuted())
                return;
        }

        VCEventType vce = null;

        if (join != null && left == null) {
            vce = VCEventType.JOIN;
        } else if (join == null) {
            vce = VCEventType.LEAVE;
        } else if (join.getIdLong() == ti.getAudioChannel() && left.getIdLong() != ti.getAudioChannel()) {
            vce = VCEventType.MOVE_FROM;
        } else if (join.getIdLong() != ti.getAudioChannel() && left.getIdLong() == ti.getAudioChannel()) {
            vce = VCEventType.MOVE_TO;
        }

        sayVCEvent(vce, ti, vt.createVoice(guildId, userId), member, join, left);
    }

    private void sayVCEvent(VCEventType vcEventType, TTSInstance ttsInstance, Voice voice, Member member, AudioChannelUnion join, AudioChannelUnion left) {
        if (vcEventType == null) return;

        ttsInstance.sayText(SaidText.vcEvent(voice, vcEventType, member, join, left));
    }
}
