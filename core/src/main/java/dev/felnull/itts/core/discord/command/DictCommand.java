package dev.felnull.itts.core.discord.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dev.felnull.itts.core.dict.Dictionary;
import dev.felnull.itts.core.dict.DictionaryManager;
import dev.felnull.itts.core.dict.ServerDictionary;
import dev.felnull.itts.core.savedata.SaveDataManager;
import dev.felnull.itts.core.savedata.legacy.LegacyDictData;
import dev.felnull.itts.core.savedata.legacy.LegacySaveDataLayer;
import dev.felnull.itts.core.util.PatternValidator;
import dev.felnull.itts.core.util.StringUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.InteractionContextType;
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
     * 最大文字数
     */
    private static final int MAX_TEXT_LENGTH = 1000;

    /**
     * フィールドに表示可能な最大文字数
     */
    private static final int MAX_FIELD_TEXT_LENGTH = 125;

    /**
     * アップロードファイルの最大サイズ (1MB)
     */
    private static final int MAX_UPLOAD_FILE_SIZE = 1024 * 1024;

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
                .setContexts(InteractionContextType.GUILD)
                .setDefaultPermissions(OWNERS_PERMISSIONS)
                .addSubcommands(new SubcommandData("toggle", "辞書ごとの有効無効の切り替え")
                        .addOption(OptionType.STRING, "name", "辞書", true, true)
                        .addOption(OptionType.BOOLEAN, "enable", "True: 有効、False: 無効", true))
                .addSubcommands(new SubcommandData("toggle-show", "辞書ごとの有効無効の表示"))
                .addSubcommands(new SubcommandData("show", "サーバー読み上げ辞書の内容を表示")
                        .addOption(OptionType.STRING, "name", "表示する辞書", true, true))
                .addSubcommands(new SubcommandData("add", "サーバー読み上げ辞書に単語を登録")
                        .addOptions(new OptionData(OptionType.STRING, "word", "対象の単語", true)
                                .setMaxLength(1000))
                        .addOptions(new OptionData(OptionType.STRING, "reading", "対象の読み", true)
                                .setMaxLength(1000))
                )
                .addSubcommands(new SubcommandData("remove", "サーバー読み上げ辞書から単語を削除")
                        .addOptions(new OptionData(OptionType.STRING, "word", "対象の単語", true, true)
                                .setMaxLength(1000)))
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
            replayEmbedBuilder.setDescription("登録なし");
        } else {
            show.entrySet().stream()
                    .limit(MessageEmbed.MAX_FIELD_AMOUNT)
                    .forEach(entry -> {
                        addDictWordAndReadingField(replayEmbedBuilder, entry.getKey(), entry.getValue());
                    });

            if (show.size() >= 2) {
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("計%d個", show.size()));

                int omit = show.size() - MessageEmbed.MAX_FIELD_AMOUNT;
                if (omit > 0) {
                    sb.append(String.format(" (%d個省略)", omit));

                    if (dic instanceof ServerDictionary) {
                        // TODO downloadコマンドの実行権限が無い場合は表示しないようにする。
                        sb.append("\n");
                        sb.append("全てのエントリを確認するには「/dict download」を実行してください。");
                    }
                }

                replayEmbedBuilder.setFooter(sb.toString());
            }
        }

        event.replyEmbeds(replayEmbedBuilder.build()).setEphemeral(true).queue();
    }

    private void upload(SlashCommandInteractionEvent event) {
        Guild guild = Objects.requireNonNull(event.getGuild());

        long guildId = guild.getIdLong();

        Message.Attachment file = Objects.requireNonNull(event.getOption("file", OptionMapping::getAsAttachment));
        boolean overwrite = Objects.requireNonNull(event.getOption("overwrite", OptionMapping::getAsBoolean));

        if (file.getSize() > MAX_UPLOAD_FILE_SIZE) {
            int maxSizeMB = MAX_UPLOAD_FILE_SIZE / (1024 * 1024);
            event.reply(String.format("ファイルサイズが大きすぎます。最大%dMBまでです。", maxSizeMB)).setEphemeral(true).queue();
            return;
        }

        event.deferReply().queue();

        file.getProxy().download().thenApplyAsync(stream -> {
            try (InputStream st = new BufferedInputStream(stream);
                 Reader reader = new InputStreamReader(st, StandardCharsets.UTF_8)) {
                JsonObject result = GSON.fromJson(reader, JsonObject.class);
                if (result == null) {
                    throw new RuntimeException("Invalid JSON file");
                }
                return result;
            } catch (JsonSyntaxException e) {
                throw new RuntimeException("Invalid JSON format: " + e.getMessage());
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
                String errorMessage = error.getCause() != null ? error.getCause().getMessage() : error.getMessage();
                event.getHook().sendMessage("辞書ファイルの読み込み中にエラーが発生しました: " + errorMessage).queue();
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

        if (word.isBlank() || reading.isBlank()) {
            event.reply("単語と読みを空にすることはできません。").setEphemeral(true).queue();
            return;
        }

        if (word.length() > MAX_TEXT_LENGTH || reading.length() > MAX_TEXT_LENGTH) {
            event.reply(String.format("登録可能な最大文字数は%d文字です", MAX_TEXT_LENGTH)).setEphemeral(true).queue();
            return;
        }

        PatternValidator.ValidationResult validationResult = PatternValidator.validate(word);
        if (!validationResult.valid()) {
            event.reply(validationResult.error()).setEphemeral(true).queue();
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
        String w;
        String r;

        if (word.length() < MAX_FIELD_TEXT_LENGTH) {
            w = "` " + word.replace("\n", "\\n") + " `";
        } else {
            w = "` " + word.substring(0, MAX_FIELD_TEXT_LENGTH).replace("\n", "\\n") + "... `";
        }

        if (reading.length() < MAX_FIELD_TEXT_LENGTH) {
            r = "```" + reading.replace("```", "\\```") + "```";
        } else {
            r = "```" + reading.substring(0, MAX_FIELD_TEXT_LENGTH).replace("```", "\\```") + "... ```";
        }
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