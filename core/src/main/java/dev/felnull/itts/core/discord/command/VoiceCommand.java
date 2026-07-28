package dev.felnull.itts.core.discord.command;

import com.google.common.collect.ImmutableList;
import dev.felnull.itts.core.ITTSRuntime;
import dev.felnull.itts.core.savedata.SaveDataManager;
import dev.felnull.itts.core.savedata.legacy.LegacySaveDataLayer;
import dev.felnull.itts.core.savedata.legacy.LegacyServerUserData;
import dev.felnull.itts.core.util.DiscordUtils;
import dev.felnull.itts.core.util.StringUtils;
import dev.felnull.itts.core.voice.VoiceCategory;
import dev.felnull.itts.core.voice.VoiceManager;
import dev.felnull.itts.core.voice.VoiceType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
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
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * 音声コマンド
 *
 * @author MORIMORI0317
 */
public class VoiceCommand extends BaseCommand {

    /**
     * 声カテゴリオプション名
     */
    private static final String VOICE_CATEGORY_OPTION_NAME = "voice_category";

    /**
     * 声モデルオプション名
     */
    private static final String VOICE_TYPE_OPTION_NAME = "voice_type";

    /**
     * 喋り型オプション名
     */
    private static final String VOICE_STYLE_OPTION_NAME = "voice_style";

    /**
     * コンストラクタ
     */
    public VoiceCommand() {
        super("voice");
    }

    @NotNull
    @Override
    public SlashCommandData createSlashCommand() {
        return Commands.slash("voice", "自分の読み上げ音声タイプ関係")
                .setContexts(InteractionContextType.GUILD)
                .setDefaultPermissions(MEMBERS_PERMISSIONS)
                .addSubcommands(new SubcommandData("change", "自分の読み上げ音声タイプを変更")
                        .addOptions(new OptionData(OptionType.STRING, VOICE_CATEGORY_OPTION_NAME, "読み上げ音声タイプのカテゴリ")
                                .setAutoComplete(true)
                                .setRequired(true))
                        .addOptions(new OptionData(OptionType.STRING, VOICE_TYPE_OPTION_NAME, "読み上げ音声モデル")
                                .setAutoComplete(true)
                                .setRequired(true))
                        .addOptions(new OptionData(OptionType.STRING, VOICE_STYLE_OPTION_NAME, "喋り型")
                                .setAutoComplete(true)))
                .addSubcommands(new SubcommandData("check", "自分の読み上げ音声タイプを確認"))
                .addSubcommands(new SubcommandData("show", "読み上げ音声タイプ一覧を表示"));
    }

    @Override
    public void commandInteraction(SlashCommandInteractionEvent event) {
        switch (Objects.requireNonNull(event.getSubcommandName())) {
            case "change" -> change(event, null);
            case "check" -> check(event, null);
            case "show" -> show(event);
            default -> {
            }
        }
    }

    private void show(SlashCommandInteractionEvent event) {
        Guild guild = Objects.requireNonNull(event.getGuild());

        EmbedBuilder showEmbedBuilder = new EmbedBuilder();
        showEmbedBuilder.setColor(getConfigManager().getConfig().getThemeColor());
        showEmbedBuilder.setTitle("読み上げ音声タイプ一覧");

        VoiceManager vm = getVoiceManager();
        VoiceType currentVt = vm.getVoiceType(guild.getIdLong(), event.getUser().getIdLong());
        VoiceType defaultVt = vm.getDefaultVoiceType(event.getGuild().getIdLong());
        Map<VoiceCategory, List<VoiceType>> catAndTypes = vm.getAvailableVoiceTypes();
        catAndTypes.forEach((cat, types) -> {
            StringBuilder typeText = new StringBuilder();

            for (VoiceType type : types) {
                String name = type.getName();

                if (defaultVt != null && type.getId().equals(defaultVt.getId())) {
                    name += " [デフォルト]";
                }

                if (currentVt != null && type.getId().equals(currentVt.getId())) {
                    name += " [使用中]";
                }

                typeText.append(name).append("\n");
            }

            showEmbedBuilder.addField(cat.getName(), typeText.toString(), false);
        });

        event.replyEmbeds(showEmbedBuilder.build()).setEphemeral(true).queue();
    }

    /**
     * 音声変更
     *
     * @param event イベント
     * @param user  ユーザ
     */
    protected static void change(SlashCommandInteractionEvent event, User user) {
        Guild guild = Objects.requireNonNull(event.getGuild());

        boolean mine = user == null;
        if (mine) {
            user = event.getUser();
        }

        LegacySaveDataLayer legacySaveDataLayer = SaveDataManager.getInstance().getLegacySaveDataLayer();

        LegacyServerUserData serverUserData = legacySaveDataLayer.getServerUserData(guild.getIdLong(), user.getIdLong());
        OptionMapping odVc = Objects.requireNonNull(event.getOption(VOICE_CATEGORY_OPTION_NAME));
        OptionMapping odVt = Objects.requireNonNull(event.getOption(VOICE_TYPE_OPTION_NAME));
        String styleId = event.getOption(VOICE_STYLE_OPTION_NAME, OptionMapping::getAsString);

        VoiceManager vm = ITTSRuntime.getInstance().getVoiceManager();
        Optional<VoiceCategory> cat = vm.getVoiceCategory(odVc.getAsString());

        if (cat.isEmpty()) {
            event.reply("存在しない読み上げカテゴリーです。").setEphemeral(true).queue();
            return;
        }

        Optional<VoiceType> vt = getVoiceType(cat.get(), odVt.getAsString(), styleId, vm.getAvailableVoiceTypes());

        if (vt.isEmpty()) {
            event.reply("存在しない読み上げタイプです。").setEphemeral(true).queue();
            return;
        }

        if (vt.get().getId().equals(serverUserData.getVoiceType())) {
            event.reply("自分の読み上げ音声タイプは変更されませんでした。").setEphemeral(true).queue();
            return;
        }

        serverUserData.setVoiceType(vt.get().getId());

        String userName = mine ? "自分" : DiscordUtils.getEscapedName(guild, user);
        event.reply(userName + "の読み上げ音声タイプを" + vt.get().getName() + "に変更しました。").setEphemeral(mine).queue();
    }

    /**
     * 音声確認
     *
     * @param event イベント
     * @param user  ユーザ
     */
    protected static void check(SlashCommandInteractionEvent event, User user) {
        Guild guild = Objects.requireNonNull(event.getGuild());

        boolean mine = user == null;
        if (mine) {
            user = event.getUser();
        }

        LegacySaveDataLayer legacySaveDataLayer = SaveDataManager.getInstance().getLegacySaveDataLayer();
        LegacyServerUserData serverUserData = legacySaveDataLayer.getServerUserData(guild.getIdLong(), user.getIdLong());
        VoiceManager vm = ITTSRuntime.getInstance().getVoiceManager();
        Optional<VoiceType> vt = vm.getVoiceType(serverUserData.getVoiceType());

        String type = vt.map(VoiceType::getName).orElseGet(() -> {
            VoiceType dvt = vm.getDefaultVoiceType(guild.getIdLong());

            if (dvt != null) {
                return dvt.getName() + " [デフォルト]";
            }

            return "無効";
        });

        String userName = mine ? "自分" : DiscordUtils.getEscapedName(guild, user);
        event.reply(userName + "の現在の読み上げタイプは" + type + "です。").setEphemeral(mine).queue();
    }

    @Override
    public void autoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        Objects.requireNonNull(event.getGuild());
        CommandAutoCompleteInteraction interact = event.getInteraction();

        if (!"change".equals(interact.getSubcommandName())) {
            return;
        }

        voiceSelectComplete(event, null, true);
    }

    /**
     * 音声選択補完
     *
     * @param event    イベント
     * @param user     ユーザ
     * @param showUsed 使用中の音声を表示するかどうか
     */
    protected static void voiceSelectComplete(CommandAutoCompleteInteractionEvent event, User user, boolean showUsed) {
        Guild guild = Objects.requireNonNull(event.getGuild());

        if (user == null) {
            user = event.getUser();
        }

        CommandAutoCompleteInteraction interact = event.getInteraction();

        AutoCompleteQuery fcs = interact.getFocusedOption();
        String val = fcs.getValue();

        VoiceManager vm = ITTSRuntime.getInstance().getVoiceManager();
        Map<VoiceCategory, List<VoiceType>> catAndTypes = vm.getAvailableVoiceTypes();

        if (VOICE_CATEGORY_OPTION_NAME.equals(fcs.getName())) {

            event.replyChoices(catAndTypes.keySet().stream()
                    .sorted(Comparator.comparingInt(cat -> -StringUtils.getComplementPoint(cat.getName(), val)))
                    .limit(OptionData.MAX_CHOICES)
                    .map(cat -> new Command.Choice(cat.getName(), cat.getId()))
                    .toList()).queue();

        } else if (VOICE_TYPE_OPTION_NAME.equals(fcs.getName())) {
            VoiceType currentVt = showUsed ? vm.getVoiceType(guild.getIdLong(), user.getIdLong()) : null;

            VoiceType defaultVt = vm.getDefaultVoiceType(guild.getIdLong());

            Optional<VoiceCategory> cat = Optional.ofNullable(event.getOption(VOICE_CATEGORY_OPTION_NAME))
                    .flatMap(catOp -> vm.getVoiceCategory(catOp.getAsString()));

            event.replyChoices(cat.map(catAndTypes::get)
                    .map(vts -> getModels(vts).stream()
                            .sorted(Comparator.comparingInt(vt -> -StringUtils.getComplementPoint(vt.getModelName(), val)))
                            .limit(OptionData.MAX_CHOICES)
                            .map(vt -> {
                                String name = vt.getModelName();
                                if (defaultVt != null && vt.getModelId().equals(defaultVt.getModelId())) {
                                    name += " [デフォルト]";
                                }

                                if (showUsed && currentVt != null && vt.getModelId().equals(currentVt.getModelId())) {
                                    name += " [使用中]";
                                }

                                return new Command.Choice(name, vt.getModelId());
                            })
                            .toList())
                    .orElseGet(ImmutableList::of)).queue();
        } else if (VOICE_STYLE_OPTION_NAME.equals(fcs.getName())) {
            VoiceType currentVt = showUsed ? vm.getVoiceType(guild.getIdLong(), user.getIdLong()) : null;

            VoiceType defaultVt = vm.getDefaultVoiceType(guild.getIdLong());

            Optional<VoiceCategory> cat = Optional.ofNullable(event.getOption(VOICE_CATEGORY_OPTION_NAME))
                    .flatMap(catOp -> vm.getVoiceCategory(catOp.getAsString()));
            Optional<String> modelId = Optional.ofNullable(event.getOption(VOICE_TYPE_OPTION_NAME))
                    .map(OptionMapping::getAsString);

            event.replyChoices(cat.map(catAndTypes::get)
                    .stream()
                    .flatMap(Collection::stream)
                    .filter(vt -> modelId.map(vt.getModelId()::equals).orElse(false))
                    .sorted(Comparator.comparingInt(vt -> -StringUtils.getComplementPoint(vt.getStyleName(), val)))
                    .limit(OptionData.MAX_CHOICES)
                    .map(vt -> {
                        String name = vt.getStyleName();
                        if (defaultVt != null && vt.getId().equals(defaultVt.getId())) {
                            name += " [デフォルト]";
                        }

                        if (showUsed && currentVt != null && vt.getId().equals(currentVt.getId())) {
                            name += " [使用中]";
                        }

                        return new Command.Choice(name, vt.getStyleId());
                    })
                    .toList()).queue();
        }
    }

    /**
     * 声タイプを取得
     *
     * @param category    声カテゴリ
     * @param modelId     声モデルID
     * @param styleId     喋り型ID
     * @param catAndTypes カテゴリ別声タイプリスト
     * @return 声タイプ
     */
    protected static Optional<VoiceType> getVoiceType(VoiceCategory category, String modelId, String styleId, Map<VoiceCategory, List<VoiceType>> catAndTypes) {
        List<VoiceType> categoryVoiceTypes = catAndTypes.getOrDefault(category, ImmutableList.of());
        Optional<VoiceType> exactVoiceType = categoryVoiceTypes.stream()
                .filter(vt -> vt.getId().equals(modelId))
                .findFirst();

        if (styleId == null && exactVoiceType.isPresent()) {
            return exactVoiceType;
        }

        List<VoiceType> modelVoiceTypes = categoryVoiceTypes.stream()
                .filter(vt -> vt.getModelId().equals(modelId) || exactVoiceType.map(vt::equals).orElse(false))
                .toList();

        if (styleId == null) {
            return modelVoiceTypes.stream().findFirst();
        }

        return modelVoiceTypes.stream()
                .filter(vt -> vt.getStyleId().equals(styleId) || vt.getId().equals(styleId))
                .findFirst();
    }

    private static List<VoiceType> getModels(List<VoiceType> voiceTypes) {
        Map<String, VoiceType> models = new LinkedHashMap<>();

        for (VoiceType voiceType : voiceTypes) {
            models.putIfAbsent(voiceType.getModelId(), voiceType);
        }

        return models.values().stream()
                .toList();
    }
}
