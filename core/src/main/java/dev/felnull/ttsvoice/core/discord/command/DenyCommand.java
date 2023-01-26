package dev.felnull.ttsvoice.core.discord.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

public class DenyCommand extends BaseCommand {
    public DenyCommand() {
        super("deny");
    }

    @NotNull
    @Override
    public SlashCommandData createSlashCommand() {
        return Commands.slash("deny", "読み上げ拒否関係")
                .setGuildOnly(true)
                .setDefaultPermissions(OWNERS_PERMISSIONS)
                .addSubcommands(new SubcommandData("add", "読み上げ拒否リストにユーザーを追加")
                        .addOptions(new OptionData(OptionType.USER, "user", "ユーザー指定")
                                .setRequired(true)))
                .addSubcommands(new SubcommandData("remove", "読み上げ拒否リストからユーザーを削除")
                        .addOptions(new OptionData(OptionType.USER, "user", "ユーザー指定")
                                .setRequired(true)))
                .addSubcommands(new SubcommandData("show", "読み上げ拒否リストを表示"));
    }

    @Override
    public void commandInteraction(SlashCommandInteractionEvent event) {

    }
}
