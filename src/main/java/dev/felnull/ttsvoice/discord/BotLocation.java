package dev.felnull.ttsvoice.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;

/**
 * BOTとサーバー情報
 *
 * @param botUserId
 * @param guildId
 */
public record BotLocation(long botUserId, long guildId) {
    public static BotLocation of(JDA jda, Guild guild) {
        return new BotLocation(jda.getSelfUser().getIdLong(), guild.getIdLong());
    }

    public static BotLocation of(Event event, Guild guild) {
        return of(event.getJDA(), guild);
    }

    public static BotLocation of(GenericInteractionCreateEvent event) {
        return of(event, event.getGuild());
    }

    public static BotLocation of(GuildVoiceUpdateEvent event) {
        return of(event.getJDA(), event.getGuild());
    }

    public static BotLocation of(GenericMessageEvent event) {
        return of(event, event.getGuild());
    }

    public JDA getJDA() {
        return JDAManager.getInstance().getJDA(botUserId);
    }

    public Guild getGuild() {
        return getJDA().getGuildById(guildId);
    }
}
