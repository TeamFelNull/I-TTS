package dev.felnull.itts.core.discord.command;

import dev.felnull.itts.core.TTSVoiceRuntime;
import dev.felnull.itts.core.util.StringUtils;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Objects;

public class DictCommand extends BaseCommand {
    public DictCommand() {
        super("dict");
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
        switch (Objects.requireNonNull(event.getSubcommandName())) {
            case "toggle" -> toggle(event);
        }
    }

    private void toggle(SlashCommandInteractionEvent event) {
        String dictId = event.getOption("name", OptionMapping::getAsString);
        if (dictId == null) dictId = "";
        boolean enabled = Boolean.TRUE.equals(event.getOption("enable", OptionMapping::getAsBoolean));
        var enStr = enabled ? "有効" : "無効";
        long guildId = event.getGuild().getIdLong();
        var dm = TTSVoiceRuntime.getInstance().getDictionaryManager();
        var sm = TTSVoiceRuntime.getInstance().getSaveDataManager();
        var dic = dm.getDictionary(dictId, guildId);

        if (dic == null) {
            event.reply("存在しない辞書です。").setEphemeral(true).queue();
            return;
        }

        var dud = sm.getDictUseData(guildId, dictId);
        if ((dud == null && !enabled) || (dud != null && enabled)) {
            event.reply(dic.getName() + "は既に" + enStr + "です。").setEphemeral(true).queue();
            return;
        }

        if (enabled) {
            sm.addDictUseData(guildId, dictId, 0);
        } else {
            sm.removeDictUseData(guildId, dictId);
        }

        event.reply(dic.getName() + "を" + enStr + "にしました。").queue();
    }

    @Override
    public void autoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        Objects.requireNonNull(event.getGuild());
        var interact = event.getInteraction();
        var fcs = interact.getFocusedOption();

        if ("toggle".equals(interact.getSubcommandName()) && "name".equals(fcs.getName())) {
            var dm = TTSVoiceRuntime.getInstance().getDictionaryManager();
            event.replyChoices(dm.getAllDictionaries(event.getGuild().getIdLong()).stream()
                    .sorted(Comparator.comparingInt(d -> -StringUtils.getComplementPoint(d.getName(), fcs.getValue())))
                    .map(n -> new Command.Choice(n.getName(), n.getId()))
                    .toList()).queue();
        }
    }
}
