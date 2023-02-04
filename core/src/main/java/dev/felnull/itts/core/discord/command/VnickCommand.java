package dev.felnull.itts.core.discord.command;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

public class VnickCommand extends BaseCommand {
    public VnickCommand() {
        super("vnick");
    }

    @NotNull
    @Override
    public SlashCommandData createSlashCommand() {
        return Commands.slash("vnick", "自分の読み上げユーザ名を変更")
                .addOptions(new OptionData(OptionType.STRING, "name", "名前")
                        .setRequired(true))
                .setGuildOnly(true)
                .setDefaultPermissions(MEMBERS_PERMISSIONS);
    }

    @Override
    public void commandInteraction(SlashCommandInteractionEvent event) {

    }

    @Override
    public void autoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {

    }
}
