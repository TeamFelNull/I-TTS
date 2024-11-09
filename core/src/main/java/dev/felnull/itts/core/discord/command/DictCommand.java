package dev.felnull.itts.core.discord.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dev.felnull.itts.core.dict.Dictionary;
import dev.felnull.itts.core.dict.DictionaryManager;
import dev.felnull.itts.core.savedata.SaveDataManager;
import dev.felnull.itts.core.savedata.legacy.LegacyDictData;
import dev.felnull.itts.core.savedata.legacy.LegacySaveDataLayer;
import dev.felnull.itts.core.util.StringUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.CommandAutoCompleteInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * 読み上げ辞書コマンド
 *
 * @author MORIMORI0317
 */
public class DictCommand extends BaseCommand {

    /**
     * GSOUN
     */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * コンストラクタ
     */
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
                .addSubcommands(new SubcommandData("show", "サーバー読み上げ辞書の内容を表示")
                        .addOption(OptionType.STRING, "name", "表示する辞書", true, true))
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
            case "download" -> download(event);
            case "upload" -> upload(event);
            case "show" -> show(event);
            default -> {
            }
        }
    }

    private void show(SlashCommandInteractionEvent event) {
        Guild guild = Objects.requireNonNull(event.getGuild());

        String dictId = Objects.requireNonNull(event.getOption("name", OptionMapping::getAsString));
        long guildId = guild.getIdLong();
        DictionaryManager dm = getDictionaryManager();
        Dictionary dic = dm.getDictionary(dictId, guildId);

        if (dic == null) {
            event.reply("存在しない辞書です。").setEphemeral(true).queue();
            return;
        }

        EmbedBuilder replayEmbedBuilder = new EmbedBuilder();
        replayEmbedBuilder.setColor(getConfigManager().getConfig().getThemeColor());

        String title = dic.getName();

        if (dic.isBuiltIn()) {
            title += " [組み込み]";
        }

        title += " (優先度: " + dic.getDefaultPriority() + ")";

        replayEmbedBuilder.setTitle(title);

        Map<String, String> show = dic.getShowInfo(guildId);

        if (show.isEmpty()) {
            replayEmbedBuilder.addField("登録なし", "", false);
        } else {
            show.forEach((k, v) -> addDictWordAndReadingField(replayEmbedBuilder, k, v));
        }

        if (show.size() >= 2) {
            replayEmbedBuilder.setFooter("計" + show.size() + "個");
        }

        event.replyEmbeds(replayEmbedBuilder.build()).setEphemeral(true).queue();
    }

    private void upload(SlashCommandInteractionEvent event) {
        Guild guild = Objects.requireNonNull(event.getGuild());

        long guildId = guild.getIdLong();

        Message.Attachment file = Objects.requireNonNull(event.getOption("file", OptionMapping::getAsAttachment));
        boolean overwrite = Objects.requireNonNull(event.getOption("overwrite", OptionMapping::getAsBoolean));

        event.deferReply().queue();

        file.getProxy().download().thenApplyAsync(stream -> {
            try (InputStream st = new BufferedInputStream(stream); Reader reader = new InputStreamReader(st)) {
                return GSON.fromJson(reader, JsonObject.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, getHeavyExecutor()).thenApplyAsync(jo -> {
            return getDictionaryManager().serverDictLoadFromJson(jo, guildId, overwrite);
        }, getHeavyExecutor()).whenCompleteAsync((ret, error) -> {
            if (error == null) {
                if (ret.isEmpty()) {
                    event.getHook().sendMessage("新しく単語は登録されませんでした").queue();
                    return;
                }

                EmbedBuilder replayEmbedBuilder = new EmbedBuilder();
                replayEmbedBuilder.setColor(getConfigManager().getConfig().getThemeColor());

                replayEmbedBuilder.setTitle("登録された単語と読み");

                for (LegacyDictData dictData : ret) {
                    addDictWordAndReadingField(replayEmbedBuilder, dictData.getTarget(), dictData.getRead());
                }

                event.getHook().sendMessageEmbeds(replayEmbedBuilder.build()).addContent(overwrite ? "以下の単語の読みを上書き登録しました" : "以下の単語の読みを登録しました").queue();
            } else {
                getITTSLogger().error("Dictionary registration failure", error);
                event.getHook().sendMessage("辞書ファイルの読み込み中にエラーが発生しました").queue();
            }
        }, getAsyncExecutor());

    }

    private void download(SlashCommandInteractionEvent event) {
        Guild guild = Objects.requireNonNull(event.getGuild());

        long guildId = guild.getIdLong();

        LegacySaveDataLayer legacySaveDataLayer = SaveDataManager.getInstance().getLegacySaveDataLayer();
        if (legacySaveDataLayer.getAllServerDictData(guildId).isEmpty()) {
            event.reply("辞書は空です").setEphemeral(true).queue();
            return;
        }

        event.deferReply(true).queue();

        CompletableFuture.supplyAsync(() -> {

            JsonObject jo = new JsonObject();
            getDictionaryManager().serverDictSaveToJson(jo, guildId);
            return GSON.toJson(jo).getBytes(StandardCharsets.UTF_8);

        }, getHeavyExecutor()).thenAcceptAsync(data -> {

            event.getHook().sendFiles(FileUpload.fromData(data, guildId + "_dict.json")).setEphemeral(true).queue();

        }, getAsyncExecutor());
    }

    private void remove(SlashCommandInteractionEvent event) {
        Guild guild = Objects.requireNonNull(event.getGuild());

        String word = Objects.requireNonNull(event.getOption("word", OptionMapping::getAsString));

        long guildId = guild.getIdLong();
        LegacySaveDataLayer legacySaveDataLayer = SaveDataManager.getInstance().getLegacySaveDataLayer();

        LegacyDictData dictData = legacySaveDataLayer.getServerDictData(guildId, word);
        if (dictData == null) {
            event.reply("未登録の単語です").setEphemeral(true).queue();
            return;
        }

        legacySaveDataLayer.removeServerDictData(guildId, word);

        EmbedBuilder replayEmbedBuilder = new EmbedBuilder();
        replayEmbedBuilder.setColor(getConfigManager().getConfig().getThemeColor());
        replayEmbedBuilder.setTitle("削除された単語と読み");
        addDictWordAndReadingField(replayEmbedBuilder, word, dictData.getRead());

        event.replyEmbeds(replayEmbedBuilder.build()).addContent("以下の単語を辞書から削除しました").queue();
    }

    private void add(SlashCommandInteractionEvent event) {
        Guild guild = Objects.requireNonNull(event.getGuild());

        String word = Objects.requireNonNull(event.getOption("word", OptionMapping::getAsString));
        String reading = Objects.requireNonNull(event.getOption("reading", OptionMapping::getAsString));

        if (word.length() > 1000 || reading.length() > 1000) {
            event.reply("登録可能な最大文字数は1000文字です").queue();
            return;
        }

        long guildId = guild.getIdLong();
        LegacySaveDataLayer legacySaveDataLayer = SaveDataManager.getInstance().getLegacySaveDataLayer();

        boolean overwrite = legacySaveDataLayer.getServerDictData(guildId, word) != null;

        legacySaveDataLayer.addServerDictData(guildId, word, reading);

        EmbedBuilder replayEmbedBuilder = new EmbedBuilder();
        replayEmbedBuilder.setColor(getConfigManager().getConfig().getThemeColor());

        replayEmbedBuilder.setTitle("登録された単語と読み");
        addDictWordAndReadingField(replayEmbedBuilder, word, reading);

        event.replyEmbeds(replayEmbedBuilder.build()).addContent(overwrite ? "以下の単語の読みを上書き登録しました" : "以下の単語の読みを登録しました").queue();
    }

    private void addDictWordAndReadingField(EmbedBuilder builder, String word, String reading) {
        String w = "` " + word.replace("\n", "\\n") + " `";
        String r = "```" + reading.replace("```", "\\```") + "```";
        builder.addField(w, r, false);
    }

    private void toggleShow(SlashCommandInteractionEvent event) {
        Guild guild = Objects.requireNonNull(event.getGuild());

        EmbedBuilder replayEmbedBuilder = new EmbedBuilder();
        replayEmbedBuilder.setColor(getConfigManager().getConfig().getThemeColor());
        replayEmbedBuilder.setTitle("現在の辞書の切り替え状況");

        long guildId = guild.getIdLong();
        DictionaryManager dictManager = getDictionaryManager();
        List<Dictionary> allDictList = dictManager.getAllDictionaries(guildId);
        List<Dictionary> orderEnableDictList = dictManager.getAllPriorityOrderEnableDictionaries(guildId);

        allDictList.forEach(dict -> {
            int priority = orderEnableDictList.indexOf(dict);
            replayEmbedBuilder.addField(dict.getName(), priority >= 0 ? ("有効 (" + (priority + 1) + ")") : "無効", false);
        });

        event.replyEmbeds(replayEmbedBuilder.build()).setEphemeral(true).queue();
    }

    private void toggle(SlashCommandInteractionEvent event) {
        String dictId = Objects.requireNonNull(event.getOption("name", OptionMapping::getAsString));

        boolean enabled = Boolean.TRUE.equals(event.getOption("enable", OptionMapping::getAsBoolean));
        Guild guild = Objects.requireNonNull(event.getGuild());

        String enStr = enabled ? "有効" : "無効";
        long guildId = guild.getIdLong();
        DictionaryManager dm = getDictionaryManager();
        Dictionary dic = dm.getDictionary(dictId, guildId);

        if (dic == null) {
            event.reply("存在しない辞書です。").setEphemeral(true).queue();
            return;
        }

        boolean preEnable = dm.isEnable(guildId, dictId);

        if (preEnable == enabled) {
            event.reply(dic.getName() + "は既に" + enStr + "です。").setEphemeral(true).queue();
            return;
        }

        dm.setEnable(guildId, dictId, enabled);

        event.reply(dic.getName() + "を" + enStr + "にしました。").queue();
    }

    @Override
    public void autoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        LegacySaveDataLayer legacySaveDataLayer = SaveDataManager.getInstance().getLegacySaveDataLayer();
        Objects.requireNonNull(event.getGuild());
        CommandAutoCompleteInteraction interact = event.getInteraction();
        AutoCompleteQuery fcs = interact.getFocusedOption();
        long guildId = event.getGuild().getIdLong();

        if (("toggle".equals(interact.getSubcommandName()) || "show".equals(interact.getSubcommandName())) && "name".equals(fcs.getName())) {
            DictionaryManager dm = getDictionaryManager();

            event.replyChoices(dm.getAllDictionaries(guildId).stream()
                    .sorted(Comparator.comparingInt(d -> -StringUtils.getComplementPoint(d.getName(), fcs.getValue())))
                    .limit(OptionData.MAX_CHOICES)
                    .map(n -> new Command.Choice(n.getName(), n.getId()))
                    .toList()).queue();

        } else if ("remove".equals(interact.getSubcommandName()) && "word".equals(fcs.getName())) {

            event.replyChoices(legacySaveDataLayer.getAllServerDictData(guildId).stream()
                    .sorted(Comparator.comparingInt(d -> -StringUtils.getComplementPoint(d.getTarget(), fcs.getValue())))
                    .limit(OptionData.MAX_CHOICES)
                    .map(it -> new Command.Choice(it.getTarget() + " -> " + it.getRead(), it.getTarget()))
                    .toList()).queue();

        }
    }
}
