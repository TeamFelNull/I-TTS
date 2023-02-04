package dev.felnull.itts.core.discord.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import org.jetbrains.annotations.NotNull;

public class AdminCommand extends BaseCommand {
    public AdminCommand() {
        super("admin");
    }

    @NotNull
    @Override
    public SlashCommandData createSlashCommand() {
        return Commands.slash("admin", "管理者専用")
                .setGuildOnly(true)
                .setDefaultPermissions(OWNERS_PERMISSIONS)
                .addSubcommands(new SubcommandData("vnick", "他人の読み上げユーザ名を変更")
                        .addOptions(new OptionData(OptionType.STRING, "name", "名前")
                                .setRequired(true))
                        .addOptions(new OptionData(OptionType.USER, "user", "ユーザー指定")
                                .setRequired(true)))
                .addSubcommandGroups(new SubcommandGroupData("voice", "読み上げ音声タイプ関係")
                        .addSubcommands((new SubcommandData("change", "他人の読み上げ音声タイプを変更")
                                        .addOptions(new OptionData(OptionType.STRING, "voice_category", "読み上げ音声タイプのカテゴリ")
                                                .setAutoComplete(true)
                                                .setRequired(true))
                                        .addOptions(new OptionData(OptionType.STRING, "voice_type", "読み上げ音声タイプ")
                                                .setAutoComplete(true)
                                                .setRequired(true)))
                                        .addOptions(new OptionData(OptionType.USER, "user", "ユーザー指定")
                                                .setRequired(true)),
                                new SubcommandData("check", "他人の読み上げ音声タイプを確認")
                                        .addOptions(new OptionData(OptionType.USER, "user", "ユーザー指定")
                                                .setRequired(true))));
    }

    @Override
    public void commandInteraction(SlashCommandInteractionEvent event) {

    }
}
