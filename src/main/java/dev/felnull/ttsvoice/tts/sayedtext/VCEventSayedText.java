package dev.felnull.ttsvoice.tts.sayedtext;

import dev.felnull.fnjl.tuple.FNPair;
import dev.felnull.ttsvoice.util.DiscordUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class VCEventSayedText implements SayedText {
    private final EventType eventType;
    private final FNPair<Guild, Integer> guildAndBotNumber;
    private final User user;
    @Nullable
    private final GuildVoiceUpdateEvent event;



    public VCEventSayedText(EventType eventType, FNPair<Guild, Integer> guildAndBotNumber, User user) {
        this(eventType, guildAndBotNumber, user, null);
    }

    public VCEventSayedText(EventType eventType, FNPair<Guild, Integer> guildAndBotNumber, User user, @Nullable GuildVoiceUpdateEvent event) {
        this.eventType = eventType;
        this.guildAndBotNumber = guildAndBotNumber;
        this.user = user;
        this.event = event;
    }

    @Override
    public String getSayVoiceText() {
        return eventType.eventText.getText(guildAndBotNumber, user, event);
    }

    public EventType getEventType() {
        return eventType;
    }

    public GuildVoiceUpdateEvent getEvent() {
        return event;
    }

    public enum EventType {
        CONNECT((guildAndBotNumber, user, e) -> DiscordUtils.getName(guildAndBotNumber.getRight(), guildAndBotNumber.getLeft(), user, user.getIdLong()) + "が接続しました"),
        JOIN((guildAndBotNumber, user, e) -> DiscordUtils.getName(guildAndBotNumber.getRight(), guildAndBotNumber.getLeft(), user, user.getIdLong()) + "が参加しました"),
        MOVE_FROM((guildAndBotNumber, user, e) -> DiscordUtils.getName(guildAndBotNumber.getRight(), guildAndBotNumber.getLeft(), user, user.getIdLong()) + "が" + DiscordUtils.getChannelName(e.getChannelLeft(), e.getMember(), "別のチャンネル") + "から移動してきました"),
        FORCE_MOVE_FROM((guildAndBotNumber, user, e) -> DiscordUtils.getName(guildAndBotNumber.getRight(), guildAndBotNumber.getLeft(), user, user.getIdLong()) + "が" + DiscordUtils.getChannelName(e.getChannelLeft(), e.getMember(), "別のチャンネル") + "から移動させられました"),
        LEAVE((guildAndBotNumber, user, e) -> DiscordUtils.getName(guildAndBotNumber.getRight(), guildAndBotNumber.getLeft(), user, user.getIdLong()) + "が退出しました"),
        FORCE_LEAVE((guildAndBotNumber, user, e) -> DiscordUtils.getName(guildAndBotNumber.getRight(), guildAndBotNumber.getLeft(), user, user.getIdLong()) + "が切断されました"),
        MOVE_TO((guildAndBotNumber, user, e) -> DiscordUtils.getName(guildAndBotNumber.getRight(), guildAndBotNumber.getLeft(), user, user.getIdLong()) + "が" + DiscordUtils.getChannelName(e.getChannelJoined(), e.getMember(), "別のチャンネル") + "へ移動しました"),
        FORCE_MOVE_TO((guildAndBotNumber, user, e) -> DiscordUtils.getName(guildAndBotNumber.getRight(), guildAndBotNumber.getLeft(), user, user.getIdLong()) + "が" + DiscordUtils.getChannelName(e.getChannelJoined(), e.getMember(), "別のチャンネル") + "へ移動させられました");

        private final EventText eventText;

        EventType(EventText eventText) {
            this.eventText = eventText;
        }
    }

    private static interface EventText {
        String getText(FNPair<Guild, Integer> guildAndBotNumber, User user, GuildVoiceUpdateEvent event);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VCEventSayedText that = (VCEventSayedText) o;
        return eventType == that.eventType && Objects.equals(guildAndBotNumber, that.guildAndBotNumber) && Objects.equals(user, that.user) && Objects.equals(event, that.event);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventType, guildAndBotNumber, user, event);
    }
}
