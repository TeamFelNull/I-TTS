package dev.felnull.ttsvoice.core.discord.command;

import dev.felnull.ttsvoice.core.TTSVoiceRuntime;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

public class DictCommand extends BaseCommand {
    public DictCommand(@NotNull TTSVoiceRuntime runtime) {
        super(runtime, "dict");
    }

    @NotNull
    @Override
    public SlashCommandData createSlashCommand() {
        return Commands.slash("dict", "読み上げ辞書")
                .setGuildOnly(true)
                .setDefaultPermissions(OWNERS_PERMISSIONS)
                .addSubcommands(new SubcommandData("toggle", "辞書ごとの有効無効の切り替え")
                        .addOptions(new OptionData(OptionType.STRING, "name", "辞書")
                                .setAutoComplete(true)
                                .setRequired(true))
                        .addOptions(new OptionData(OptionType.BOOLEAN, "enable", "True: 有効、False: 無効")
                                .setRequired(true)))
                .addSubcommands(new SubcommandData("toggle-show", "辞書ごとの有効無効の表示"))
                .addSubcommands(new SubcommandData("show", "サーバー読み上げ辞書の内容を表示"))
                .addSubcommands(new SubcommandData("add", "サーバー読み上げ辞書に単語を登録")
                        .addOption(OptionType.STRING, "word", "対象の単語", true)
                        .addOption(OptionType.STRING, "reading", "対象の読み", true))
                .addSubcommands(new SubcommandData("remove", "サーバー読み上げ辞書から単語を削除")
                        .addOption(OptionType.STRING, "word", "対象の単語", true, true))
                .addSubcommands(new SubcommandData("download", "現在の読み上げ辞書をダウンロード"))
                .addSubcommands(new SubcommandData("upload", "読み上げ辞書をアップロード")
                        .addOption(OptionType.ATTACHMENT, "file", "辞書ファイル", true)
                        .addOption(OptionType.BOOLEAN, "overwrite", "上書き", true));
    }

    @Override
    public void commandInteraction(SlashCommandInteractionEvent event) {

    }
}
