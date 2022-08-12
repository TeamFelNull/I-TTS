package dev.felnull.ttsvoice.tts.sayedtext;

import dev.felnull.ttsvoice.discord.BotLocation;
import dev.felnull.ttsvoice.util.DiscordUtils;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class VCEventSayedText implements SayedText {
    private final EventType eventType;
    private final BotLocation botLocation;
    private final User user;
    @Nullable
    private final GuildVoiceUpdateEvent event;

    public VCEventSayedText(EventType eventType, BotLocation botLocation, User user) {
        this(eventType, botLocation, user, null);
    }

    public VCEventSayedText(EventType eventType, BotLocation botLocation, User user, @Nullable GuildVoiceUpdateEvent event) {
        this.eventType = eventType;
        this.botLocation = botLocation;
        this.user = user;
        this.event = event;
    }

    @Override
    public String getSayVoiceText() {
        return eventType.eventText.getText(botLocation, user, event);
    }

    public EventType getEventType() {
        return eventType;
    }

    @Nullable
    public GuildVoiceUpdateEvent getEvent() {
        return event;
    }

    public enum EventType {
        CONNECT((theBotLocation, user, e) -> DiscordUtils.getName(theBotLocation, user, user.getIdLong()) + "が接続しました"),
        JOIN((theBotLocation, user, e) -> DiscordUtils.getName(theBotLocation, user, user.getIdLong()) + "が参加しました"),
        MOVE_FROM((theBotLocation, user, e) -> DiscordUtils.getName(theBotLocation, user, user.getIdLong()) + "が" + DiscordUtils.getChannelName(e.getChannelLeft(), e.getMember(), "別のチャンネル") + "から移動してきました"),
        FORCE_MOVE_FROM((theBotLocation, user, e) -> DiscordUtils.getName(theBotLocation, user, user.getIdLong()) + "が" + DiscordUtils.getChannelName(e.getChannelLeft(), e.getMember(), "別のチャンネル") + "から移動させられました"),
        LEAVE((theBotLocation, user, e) -> DiscordUtils.getName(theBotLocation, user, user.getIdLong()) + "が退出しました"),
        FORCE_LEAVE((theBotLocation, user, e) -> DiscordUtils.getName(theBotLocation, user, user.getIdLong()) + "が切断されました"),
        MOVE_TO((theBotLocation, user, e) -> DiscordUtils.getName(theBotLocation, user, user.getIdLong()) + "が" + DiscordUtils.getChannelName(e.getChannelJoined(), e.getMember(), "別のチャンネル") + "へ移動しました"),
        FORCE_MOVE_TO((theBotLocation, user, e) -> DiscordUtils.getName(theBotLocation, user, user.getIdLong()) + "が" + DiscordUtils.getChannelName(e.getChannelJoined(), e.getMember(), "別のチャンネル") + "へ移動させられました");

        private final EventText eventText;

        EventType(EventText eventText) {
            this.eventText = eventText;
        }
    }

    private static interface EventText {
        String getText(BotLocation botLocation, User user, GuildVoiceUpdateEvent event);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VCEventSayedText that = (VCEventSayedText) o;
        return eventType == that.eventType && Objects.equals(botLocation, that.botLocation) && Objects.equals(user, that.user) && Objects.equals(event, that.event);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventType, botLocation, user, event);
    }
}
