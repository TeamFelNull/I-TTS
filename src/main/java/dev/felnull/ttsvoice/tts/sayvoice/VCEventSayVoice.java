package dev.felnull.ttsvoice.tts.sayvoice;

import dev.felnull.ttsvoice.util.DiscordUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class VCEventSayVoice implements ISayVoice {
    private final EventType eventType;
    private final Guild guild;
    private final User user;
    @Nullable
    private final String name;

    public VCEventSayVoice(EventType eventType, Guild guild, User user) {
        this(eventType, guild, user, null);
    }

    public VCEventSayVoice(EventType eventType, Guild guild, User user, @Nullable String name) {
        this.eventType = eventType;
        this.guild = guild;
        this.user = user;
        this.name = name;
    }

    @Override
    public String getSayVoiceText() {
        return eventType.eventText.getText(guild, user, name);
    }

    public EventType getEventType() {
        return eventType;
    }

    public @Nullable String getName() {
        return name;
    }

    public static enum EventType {
        JOIN((guild, user, name) -> DiscordUtils.getName(guild, user, user.getIdLong()) + "が参加しました"),
        LEAVE((guild, user, name) -> DiscordUtils.getName(guild, user, user.getIdLong()) + "が退出しました"),
        MOVE_TO((guild, user, name) -> DiscordUtils.getName(guild, user, user.getIdLong()) + "が" + name + "へ移動しました"),
        MOVE_FROM((guild, user, name) -> DiscordUtils.getName(guild, user, user.getIdLong()) + "が" + name + "から移動してきました");
        private final EventText eventText;

        EventType(EventText eventText) {
            this.eventText = eventText;
        }
    }

    private static interface EventText {
        String getText(Guild guild, User user, String name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VCEventSayVoice that = (VCEventSayVoice) o;
        return eventType == that.eventType && Objects.equals(guild, that.guild) && Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventType, guild, user);
    }
}
