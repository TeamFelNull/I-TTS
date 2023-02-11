package dev.felnull.itts.core.discord.command;

import dev.felnull.itts.core.dict.Dictionary;
import dev.felnull.itts.core.dict.DictionaryManager;
import dev.felnull.itts.core.savedata.DictData;
import dev.felnull.itts.core.util.StringUtils;
import net.dv8tion.jda.api.EmbedBuilder;
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
import java.util.List;
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
                        .addOption(OptionType.STRING, "name", "辞書", true, true)
                        .addOption(OptionType.BOOLEAN, "enable", "True: 有効、False: 無効", true))
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
            case "toggle-show" -> toggleShow(event);
            case "add" -> add(event);
            case "remove" -> remove(event);
        }
    }

    private void remove(SlashCommandInteractionEvent event) {
        String word = Objects.requireNonNull(event.getOption("word", OptionMapping::getAsString));

        long guildId = event.getGuild().getIdLong();
        DictData dictData = getSaveDataManager().getServerDictData(guildId, word);
        if (dictData == null) {
            event.reply("未登録の単語です").setEphemeral(true).queue();
            return;
        }

        getSaveDataManager().removeServerDictData(guildId, word);

        EmbedBuilder replayEmbedBuilder = new EmbedBuilder();
        replayEmbedBuilder.setColor(getConfigManager().getConfig().getThemeColor());
        replayEmbedBuilder.setTitle("削除された単語と読み");
        addDictWordAndReadingField(replayEmbedBuilder, word, dictData.getRead());

        event.replyEmbeds(replayEmbedBuilder.build()).addContent("以下の単語を辞書から削除しました").queue();
    }

    private void add(SlashCommandInteractionEvent event) {
        String word = Objects.requireNonNull(event.getOption("word", OptionMapping::getAsString));
        String reading = Objects.requireNonNull(event.getOption("reading", OptionMapping::getAsString));

        if (word.length() > 1000 || reading.length() > 1000) {
            event.reply("登録可能な最大文字数は1000文字です").queue();
            return;
        }

        long guildId = event.getGuild().getIdLong();
        boolean overwrite = getSaveDataManager().getServerDictData(guildId, word) != null;

        getSaveDataManager().addServerDictData(guildId, word, reading);

        EmbedBuilder replayEmbedBuilder = new EmbedBuilder();
        replayEmbedBuilder.setColor(getConfigManager().getConfig().getThemeColor());

        replayEmbedBuilder.setTitle("登録された単語と読み");
        addDictWordAndReadingField(replayEmbedBuilder, word, reading);

        event.replyEmbeds(replayEmbedBuilder.build()).addContent(overwrite ? "以下の単語の読みを上書き登録しました" : "以下の単語の読みを登録しました").queue();
    }

    private void addDictWordAndReadingField(EmbedBuilder builder, String word, String reading) {
        var w = "` " + word.replace("\n", "\\n") + " `";
        var r = "```" + reading.replace("```", "\\```") + "```";
        builder.addField(w, r, false);
    }

    private void toggleShow(SlashCommandInteractionEvent event) {
        EmbedBuilder replayEmbedBuilder = new EmbedBuilder();
        replayEmbedBuilder.setColor(getConfigManager().getConfig().getThemeColor());
        replayEmbedBuilder.setTitle("現在の辞書の切り替え状況");

        long guildId = event.getGuild().getIdLong();
        DictionaryManager dictManager = getDictionaryManager();
        List<Dictionary> dicts = dictManager.getAllDictionaries(guildId);

        for (Dictionary dict : dicts) {
            replayEmbedBuilder.addField(dict.getName(), dictManager.isEnable(dict, guildId) ? "有効" : "無効", false);
        }

        event.replyEmbeds(replayEmbedBuilder.build()).setEphemeral(true).queue();
    }

    private void toggle(SlashCommandInteractionEvent event) {
        String dictId = event.getOption("name", OptionMapping::getAsString);
        if (dictId == null) dictId = "";
        boolean enabled = Boolean.TRUE.equals(event.getOption("enable", OptionMapping::getAsBoolean));
        var enStr = enabled ? "有効" : "無効";
        long guildId = event.getGuild().getIdLong();
        var dm = getDictionaryManager();
        var sm = getSaveDataManager();
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
        long guildId = event.getGuild().getIdLong();

        if ("toggle".equals(interact.getSubcommandName()) && "name".equals(fcs.getName())) {
            var dm = getDictionaryManager();

            event.replyChoices(dm.getAllDictionaries(guildId).stream()
                    .sorted(Comparator.comparingInt(d -> -StringUtils.getComplementPoint(d.getName(), fcs.getValue())))
                    .limit(OptionData.MAX_CHOICES)
                    .map(n -> new Command.Choice(n.getName(), n.getId()))
                    .toList()).queue();

        } else if ("remove".equals(interact.getSubcommandName()) && "word".equals(fcs.getName())) {

            event.replyChoices(getSaveDataManager().getAllServerDictData(guildId).stream()
                    .sorted(Comparator.comparingInt(d -> -StringUtils.getComplementPoint(d.getTarget(), fcs.getValue())))
                    .limit(OptionData.MAX_CHOICES)
                    .map(it -> new Command.Choice(it.getTarget() + " -> " + it.getRead(), it.getTarget()))
                    .toList()).queue();

        }
    }
}
