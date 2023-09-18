package dev.felnull.itts.core.discord.command;

import dev.felnull.itts.core.savedata.ServerData;
import dev.felnull.itts.core.voice.VoiceCategory;
import dev.felnull.itts.core.voice.VoiceManager;
import dev.felnull.itts.core.voice.VoiceType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.CommandAutoCompleteInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

/**
 * Configコマンド
 *
 * @author MORIMORI0317
 */
public class ConfigCommand extends BaseCommand {

    /**
     * コンストラクタ
     */
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
                .addSubcommands(new SubcommandData("read-ignore", "読み上げない文字")
                        .addOptions(new OptionData(OptionType.STRING, "regex", "正規表現")
                                .setRequired(true)))
                .addSubcommands(new SubcommandData("default-voice", "デフォルトの読み上げタイプ")
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
            case "read-limit" -> readLimit(event);
            case "name-read-limit" -> nameReadLimit(event);
            case "need-join" -> needJoin(event);
            case "read-overwrite" -> readOverwrite(event);
            case "read-ignore" -> readIgnore(event);
            case "default-voice" -> defaultVoice(event);
            case "show" -> show(event);
            default -> {
            }
        }
    }

    private void show(SlashCommandInteractionEvent event) {
        Guild guild = Objects.requireNonNull(event.getGuild());

        EmbedBuilder showEmbedBuilder = new EmbedBuilder();
        showEmbedBuilder.setColor(getConfigManager().getConfig().getThemeColor());
        showEmbedBuilder.setTitle("現在のコンフィグ");

        ServerData sd = getSaveDataManager().getServerData(guild.getIdLong());
        VoiceManager vm = getVoiceManager();
        VoiceType dv = vm.getDefaultVoiceType(guild.getIdLong());

        final boolean inline = true;
        showEmbedBuilder.addField("VCの入退室時にユーザー名を読み上げ", sd.isNotifyMove() ? "有効" : "無効", inline);
        showEmbedBuilder.addField("読み上げ文字数上限", sd.getReadLimit() + "文字", inline);
        showEmbedBuilder.addField("名前の読み上げ文字数上限", sd.getNameReadLimit() + "文字", inline);
        showEmbedBuilder.addField("VCに参加中のユーザーのみ読み上げ", sd.isNeedJoin() ? "有効" : "無効", inline);
        showEmbedBuilder.addField("読み上げの上書き", sd.isOverwriteAloud() ? "有効" : "無効", inline);
        showEmbedBuilder.addField("読み上げない文字(正規表現)", sd.getIgnoreRegex() == null ? "無し" : ("``" + sd.getIgnoreRegex() + "``"), inline);
        showEmbedBuilder.addField("デフォルトの読み上げタイプ", dv == null ? "無し" : dv.getName(), inline);

        event.replyEmbeds(showEmbedBuilder.build()).queue();
    }

    private void defaultVoice(SlashCommandInteractionEvent event) {
        Guild guild = Objects.requireNonNull(event.getGuild());

        String odVc = event.getOption("voice_category", OptionMapping::getAsString);
        String odVt = event.getOption("voice_type", OptionMapping::getAsString);

        ServerData sd = getSaveDataManager().getServerData(guild.getIdLong());
        VoiceManager vm = getVoiceManager();
        Optional<VoiceCategory> cat = vm.getVoiceCategory(odVc);

        if (cat.isEmpty()) {
            event.reply("存在しない読み上げカテゴリーです。").setEphemeral(true).queue();
            return;
        }

        Optional<VoiceType> vt = vm.getVoiceType(odVt);

        if (vt.isEmpty()) {
            event.reply("存在しない読み上げタイプです。").setEphemeral(true).queue();
            return;
        }

        VoiceType pre = vm.getDefaultVoiceType(event.getGuild().getIdLong());

        if (pre == null || !vt.get().getId().equals(pre.getId())) {
            sd.setDefaultVoiceType(vt.get().getId());

            event.reply("デフォルトの読み上げタイプを" + vt.get().getName() + "にしました。").queue();
        } else {
            event.reply("既にデフォルトの読み上げタイプは" + vt.get().getName() + "です。").queue();
        }
    }

    private void readIgnore(SlashCommandInteractionEvent event) {
        Guild guild = Objects.requireNonNull(event.getGuild());

        String op = Objects.requireNonNull(event.getOption("regex", OptionMapping::getAsString));
        ServerData sd = getSaveDataManager().getServerData(guild.getIdLong());

        String pre = sd.getIgnoreRegex();
        if (!op.equals(pre)) {
            sd.setIgnoreRegex(op);

            event.reply("読み上げない文字を``" + op + "``にしました。").queue();
        } else {
            event.reply("既に読み上げない文字は``" + op + "``です。").queue();
        }
    }

    private void readOverwrite(SlashCommandInteractionEvent event) {
        Guild guild = Objects.requireNonNull(event.getGuild());

        boolean op = Boolean.TRUE.equals(event.getOption("enable", OptionMapping::getAsBoolean));
        ServerData sd = getSaveDataManager().getServerData(guild.getIdLong());

        boolean pre = sd.isOverwriteAloud();
        String enStr = op ? "有効" : "無効";

        if (op != pre) {
            sd.setOverwriteAloud(op);
            getTTSManager().reload(guild);

            event.reply("読み上げの上書きを" + enStr + "にしました。").queue();
        } else {
            event.reply("既に読み上げの上書きは" + enStr + "です。").queue();
        }
    }

    private void needJoin(SlashCommandInteractionEvent event) {
        Guild guild = Objects.requireNonNull(event.getGuild());
        boolean op = Boolean.TRUE.equals(event.getOption("enable", OptionMapping::getAsBoolean));
        ServerData sd = getSaveDataManager().getServerData(guild.getIdLong());

        boolean pre = sd.isNeedJoin();
        String enStr = op ? "有効" : "無効";

        if (op != pre) {
            sd.setNeedJoin(op);

            event.reply("VCに参加中のユーザーのみ読み上げを" + enStr + "にしました。").queue();
        } else {
            event.reply("既にVCに参加中のユーザーのみ読み上げは" + enStr + "です。").queue();
        }
    }

    private void nameReadLimit(SlashCommandInteractionEvent event) {
        Guild guild = Objects.requireNonNull(event.getGuild());
        int op = Objects.requireNonNullElse(event.getOption("max-count", OptionMapping::getAsInt), 0);
        ServerData sd = getSaveDataManager().getServerData(guild.getIdLong());

        int pre = sd.getNameReadLimit();
        if (op != pre) {
            sd.setNameReadLimit(op);

            event.reply("名前の読み上げ文字数上限を" + op + "文字にしました。").queue();
        } else {
            event.reply("既に名前の読み上げ文字数上限は" + op + "文字です。").queue();
        }
    }

    private void readLimit(SlashCommandInteractionEvent event) {
        Guild guild = Objects.requireNonNull(event.getGuild());

        int op = Objects.requireNonNullElse(event.getOption("max-count", OptionMapping::getAsInt), 0);
        ServerData sd = getSaveDataManager().getServerData(guild.getIdLong());

        int pre = sd.getReadLimit();
        if (op != pre) {
            sd.setReadLimit(op);

            event.reply("読み上げ文字数上限を" + op + "文字にしました。").queue();
        } else {
            event.reply("既に読み上げ文字数上限は" + op + "文字です。").queue();
        }
    }


    private void notifyMove(SlashCommandInteractionEvent event) {
        Guild guild = Objects.requireNonNull(event.getGuild());

        OptionMapping op = Objects.requireNonNull(event.getOption("enable"));
        ServerData sd = getSaveDataManager().getServerData(guild.getIdLong());

        boolean pre = sd.isNotifyMove();
        String enStr = op.getAsBoolean() ? "有効" : "無効";

        if (op.getAsBoolean() != pre) {
            sd.setNotifyMove(op.getAsBoolean());

            event.reply("VCの入退室時にユーザー名を読み上げを" + enStr + "にしました。").queue();
        } else {
            event.reply("既にVCの入退室時にユーザー名を読み上げは" + enStr + "です。").queue();
        }
    }

    @Override
    public void autoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        Objects.requireNonNull(event.getGuild());
        CommandAutoCompleteInteraction interact = event.getInteraction();

        if (!"default-voice".equals(interact.getSubcommandName())) {
            return;
        }

        VoiceCommand.voiceSelectComplete(event, null, false);
    }
}
