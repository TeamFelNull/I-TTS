package dev.felnull.ttsvoice.core.discord;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class EventListener extends ListenerAdapter {
    private final Bot bot;

    public EventListener(@NotNull Bot bot) {
        this.bot = bot;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        this.bot.baseCommands.stream().filter(n -> n.isCommandMatch(event)).forEach(r -> r.commandInteraction(event));
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        this.bot.baseCommands.stream().filter(n -> n.isAutoCompleteMatch(event)).forEach(r -> r.autoCompleteInteraction(event));
    }
}
