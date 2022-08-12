package dev.felnull.ttsvoice.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

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

    public JDA getJDA() {
        return JDAManager.getInstance().getJDA(botUserId);
    }

    public Guild getGuild() {
        return getJDA().getGuildById(guildId);
    }
}
