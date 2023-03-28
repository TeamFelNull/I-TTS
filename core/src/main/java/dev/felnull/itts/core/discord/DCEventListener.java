package dev.felnull.itts.core.discord;

import dev.felnull.itts.core.ITTSRuntimeUse;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class DCEventListener extends ListenerAdapter implements ITTSRuntimeUse {
    private final Bot bot;

    public DCEventListener(@NotNull Bot bot) {
        this.bot = bot;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        this.bot.baseCommands.stream()
                .filter(n -> n.isCommandMatch(event))
                .limit(1)
                .forEach(r -> r.commandInteraction(event));

    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        this.bot.baseCommands.stream()
                .filter(n -> n.isAutoCompleteMatch(event))
                .limit(1)
                .forEach(r -> r.autoCompleteInteraction(event));
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (getTTSManager().canSpeak(event.getGuild())) {
            getTTSManager().sayChat(event.getGuild(), event.getChannel(), event.getMember(), event.getMessage().getContentDisplay());
            getTTSManager().sayUploadFile(event.getGuild(), event.getChannel(), event.getMember(), event.getMessage().getAttachments());
        }
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        var join = event.getChannelJoined();
        var left = event.getChannelLeft();

        if (event.getMember().getUser().getIdLong() == bot.getJDA().getSelfUser().getIdLong()) {
            if (left != null)
                getTTSManager().disconnect(event.getGuild());

            if (join != null)
                getTTSManager().connect(event.getGuild(), join);
        }

        getTTSManager().onVCEvent(event.getGuild(), event.getMember(), join, left);
    }
}
