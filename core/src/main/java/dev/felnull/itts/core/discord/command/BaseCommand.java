package dev.felnull.itts.core.discord.command;

import dev.felnull.itts.core.ITTSRuntimeUse;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

public abstract class BaseCommand implements ITTSRuntimeUse {
    protected static final DefaultMemberPermissions MEMBERS_PERMISSIONS = DefaultMemberPermissions.enabledFor(Permission.VOICE_CONNECT, Permission.MESSAGE_SEND);
    protected static final DefaultMemberPermissions OWNERS_PERMISSIONS = DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER);
    @NotNull
    protected final String name;

    protected BaseCommand(@NotNull String name) {
        this.name = name;
    }

    public boolean isCommandMatch(SlashCommandInteractionEvent event) {
        return name.equals(event.getName()) && event.getGuild() != null && event.getMember() != null;
    }

    public boolean isAutoCompleteMatch(CommandAutoCompleteInteractionEvent event) {
        return name.equals(event.getName()) && event.getGuild() != null && event.getMember() != null;
    }

    @NotNull
    abstract public SlashCommandData createSlashCommand();

    abstract public void commandInteraction(SlashCommandInteractionEvent event);

    public void autoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
    }
}
