package dev.felnull.ttsvoice.core.discord.command;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ConfigCommand extends BaseCommand {
    public ConfigCommand() {
        super("config");
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
        switch (Objects.requireNonNull(event.getSubcommandName())) {
            case "notify-move" -> notifyMove(event);
            case "read-overwrite" -> readOverwrite(event);
        }
    }

    private void readOverwrite(SlashCommandInteractionEvent event) {
        var op = Objects.requireNonNull(event.getOption("enable"));
        var sd = getRuntime().getSaveDataManager().getServerData(event.getGuild().getIdLong());

        boolean pre = sd.isOverwriteAloud();
        String enStr = op.getAsBoolean() ? "有効" : "無効";

        if (op.getAsBoolean() != pre) {
            sd.setOverwriteAloud(op.getAsBoolean());
            getRuntime().getTTSManager().reload(event.getGuild());

            event.reply("読み上げの上書きを" + enStr + "にしました").queue();
        } else {
            event.reply("すでに読み上げの上書きは" + enStr + "です。").queue();
        }
    }

    private void notifyMove(SlashCommandInteractionEvent event) {
        /*var op = Objects.requireNonNull(event.getOption("enable"));

        var sd = getRuntime().getSaveDataManager().getServerData(event.getGuild().getIdLong());
        sd.setNotifyMove(op.getAsBoolean());

        event.reply(String.valueOf(sd.isNotifyMove())).queue();*/
    }

    @Override
    public void autoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {

    }
}
