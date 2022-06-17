package dev.felnull.ttsvoice.tts.sayvoice;

import dev.felnull.ttsvoice.util.DiscordUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class VCEventSayVoice implements ISayVoice {
    private final EventType eventType;
    private final Guild guild;
    private final User user;

    public VCEventSayVoice(EventType eventType, Guild guild, User user) {
        this.eventType = eventType;
        this.guild = guild;
        this.user = user;
    }

    @Override
    public String getSayVoiceText() {
        return eventType.eventText.getText(guild, user);
    }

    public EventType getEventType() {
        return eventType;
    }

    public static enum EventType {
        JOIN((guild, user) -> DiscordUtils.getName(guild, user, user.getIdLong()) + "が参加しました"),
        LEAVE((guild, user) -> DiscordUtils.getName(guild, user, user.getIdLong()) + "が退出しました");
        private final EventText eventText;

        EventType(EventText eventText) {
            this.eventText = eventText;
        }
    }

    private static interface EventText {
        String getText(Guild guild, User user);
    }
}
