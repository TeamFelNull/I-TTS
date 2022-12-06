package dev.felnull.ttsvoice.core.discord.command;

import dev.felnull.ttsvoice.core.TTSVoiceRuntime;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

public class ConfigCommand extends BaseCommand {
    public ConfigCommand(@NotNull TTSVoiceRuntime runtime) {
        super(runtime, "config");
    }

    @NotNull
    @Override
    public SlashCommandData createSlashCommand() {
        return Commands.slash("config", "設定")
                .setGuildOnly(true)
                .setDefaultPermissions(OWNERS_PERMISSIONS)
                .addSubcommands(new SubcommandData("notify-move", "VCの入退室時にユーザー名を読み上げ")
                        .addOptions(new OptionData(OptionType.BOOLEAN, "enable", "True: 有効、False: 無効")
                                .setRequired(true)))
                .addSubcommands(new SubcommandData("read-limit", "読み上げ文字数上限")
                        .addOptions(new OptionData(OptionType.INTEGER, "max-count", "最大文字数")
                                .setMinValue(1)
                                .setMaxValue(Integer.MAX_VALUE)
                                .setRequired(true)))
                .addSubcommands(new SubcommandData("name-read-limit", "名前の読み上げ文字数上限")
                        .addOptions(new OptionData(OptionType.INTEGER, "max-count", "最大文字数")
                                .setMinValue(1)
                                .setMaxValue(Integer.MAX_VALUE)
                                .setRequired(true)))
                .addSubcommands(new SubcommandData("need-join", "VCに参加中のユーザーのみ読み上げ")
                        .addOptions(new OptionData(OptionType.BOOLEAN, "enable", "True: 有効、False: 無効")
                                .setRequired(true)))
                .addSubcommands(new SubcommandData("read-overwrite", "読み上げの上書き")
                        .addOptions(new OptionData(OptionType.BOOLEAN, "enable", "True: 有効、False: 無効")
                                .setRequired(true)))
                .addSubcommands(new SubcommandData("read-ignore", "読み上げない文字(正規表現)")
                        .addOptions(new OptionData(OptionType.BOOLEAN, "enable", "True: 有効、False: 無効")
                                .setRequired(true)))
                .addSubcommands(new SubcommandData("default-voice", "初期の読み上げタイプ")
                        .addOptions(new OptionData(OptionType.STRING, "voice_category", "読み上げ音声タイプのカテゴリ")
                                .setAutoComplete(true)
                                .setRequired(true))
                        .addOptions(new OptionData(OptionType.STRING, "voice_type", "読み上げ音声タイプ")
                                .setAutoComplete(true)
                                .setRequired(true)))
                .addSubcommands(new SubcommandData("show", "現在のコンフィグを表示"));
    }

    @Override
    public void commandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getSubcommandName()) {
            case "notify-move" -> notifyMove(event);
        }
    }

    private void notifyMove(SlashCommandInteractionEvent event) {
        var op = event.getOption("enable");
        if (op.getType() == OptionType.BOOLEAN) {
            var sd = runtime.getSaveDataManager().getServerData(event.getGuild().getIdLong());
            sd.setNotifyMove(op.getAsBoolean());
        }
    }

    @Override
    public void autoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {

    }
}
