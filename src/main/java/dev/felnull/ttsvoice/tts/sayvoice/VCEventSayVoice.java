package dev.felnull.ttsvoice.tts.sayvoice;

import dev.felnull.fnjl.tuple.FNPair;
import dev.felnull.ttsvoice.util.DiscordUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class VCEventSayVoice implements ISayVoice {
    private final EventType eventType;
    private final FNPair<Guild, Integer> guildAndBotNumber;
    private final User user;
    @Nullable
    private final String name;

    public VCEventSayVoice(EventType eventType, FNPair<Guild, Integer> guildAndBotNumber, User user) {
        this(eventType, guildAndBotNumber, user, null);
    }

    public VCEventSayVoice(EventType eventType, FNPair<Guild, Integer> guildAndBotNumber, User user, @Nullable String name) {
        this.eventType = eventType;
        this.guildAndBotNumber = guildAndBotNumber;
        this.user = user;
        this.name = name;
    }

    @Override
    public String getSayVoiceText() {
        return eventType.eventText.getText(guildAndBotNumber, user, name);
    }

    public EventType getEventType() {
        return eventType;
    }

    public @Nullable String getName() {
        return name;
    }

    public static enum EventType {
        JOIN((guildAndBotNumber, user, name) -> DiscordUtils.getName(guildAndBotNumber.getRight(), guildAndBotNumber.getLeft(), user, user.getIdLong()) + "が参加しました"),
        LEAVE((guildAndBotNumber, user, name) -> DiscordUtils.getName(guildAndBotNumber.getRight(), guildAndBotNumber.getLeft(), user, user.getIdLong()) + "が退出しました"),
        MOVE_TO((guildAndBotNumber, user, name) -> DiscordUtils.getName(guildAndBotNumber.getRight(), guildAndBotNumber.getLeft(), user, user.getIdLong()) + "が" + name + "へ移動しました"),
        MOVE_FROM((guildAndBotNumber, user, name) -> DiscordUtils.getName(guildAndBotNumber.getRight(), guildAndBotNumber.getLeft(), user, user.getIdLong()) + "が" + name + "から移動してきました");
        private final EventText eventText;

        EventType(EventText eventText) {
            this.eventText = eventText;
        }
    }

    private static interface EventText {
        String getText(FNPair<Guild, Integer> guildAndBotNumber, User user, String name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VCEventSayVoice that = (VCEventSayVoice) o;
        return eventType == that.eventType && Objects.equals(guildAndBotNumber, that.guildAndBotNumber) && Objects.equals(user, that.user) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventType, guildAndBotNumber, user, name);
    }
}
