package dev.felnull.ttsvoice.tts;

import dev.felnull.ttsvoice.Main;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

public record BotAndGuild(int botNumber, long guildId) {
    public JDA getJDA() {
        return Main.getJDA(botNumber);
    }

    public Guild getGuild() {
        return getJDA().getGuildById(guildId);
    }
}