package dev.felnull.itts.core.discord.command;

import dev.felnull.itts.core.savedata.SaveDataManager;
import dev.felnull.itts.core.savedata.repository.DataRepository;
import dev.felnull.itts.core.savedata.repository.TTSCountData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Objects;

/**
 * 読み上げ統計表示コマンド
 */
public class StatCommand extends BaseCommand {

    /**
     * 時間を相対的に表示するフォーマット
     */
    private static final String RELATIVE_TIME_FORMAT = "<t:%d:R>";

    /**
     * コンストラクタ
     */
    public StatCommand() {
        super("stat");
    }

    @NotNull
    @Override
    public SlashCommandData createSlashCommand() {
        return Commands.slash("stat", "読み上げ統計を表示")
                .setContexts(InteractionContextType.GUILD)
                .setDefaultPermissions(OWNERS_PERMISSIONS)
                .addSubcommands(new SubcommandData("today", "本日のBOT全体の読み上げ統計"))
                .addSubcommands(new SubcommandData("week", "過去7日のBOT全体の読み上げ統計"))
                .addSubcommands(new SubcommandData("all", "BOT全体の累計統計"))
                .addSubcommands(new SubcommandData("server", "現在のサーバーの累計統計"));
    }

    @Override
    public void commandInteraction(SlashCommandInteractionEvent event) {
        switch (Objects.requireNonNull(event.getSubcommandName())) {
            case "today" -> today(event);
            case "week" -> week(event);
            case "all" -> all(event);
            case "server" -> server(event);
            default -> {
            }
        }
    }

    private void today(SlashCommandInteractionEvent event) {
        DataRepository repo = SaveDataManager.getInstance().getRepository();
        long botId = event.getJDA().getSelfUser().getIdLong();
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        TTSCountData data = repo.getGlobalTTSCount(botId, today);

        EmbedBuilder builder = baseEmbed("本日の読み上げ統計 (UTC)");
        builder.addField("文字数", data.getCharCount() + "文字", true);
        builder.addField("メッセージ数", data.getMessageCount() + "件", true);
        addUptimeFields(builder);

        event.replyEmbeds(builder.build()).setEphemeral(true).queue();
    }

    private void week(SlashCommandInteractionEvent event) {
        DataRepository repo = SaveDataManager.getInstance().getRepository();
        long botId = event.getJDA().getSelfUser().getIdLong();
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate from = today.minusDays(6);

        long charSum = repo.sumGlobalCharCount(botId, from, today);

        EmbedBuilder builder = baseEmbed("過去7日の読み上げ統計 (UTC)");
        builder.addField("期間", from + " - " + today, false);
        builder.addField("文字数", charSum + "文字", true);
        addUptimeFields(builder);

        event.replyEmbeds(builder.build()).setEphemeral(true).queue();
    }

    private void all(SlashCommandInteractionEvent event) {
        DataRepository repo = SaveDataManager.getInstance().getRepository();
        long botId = event.getJDA().getSelfUser().getIdLong();

        long charSum = repo.sumGlobalAllCharCount(botId);
        long messageSum = repo.sumGlobalAllMessageCount(botId);

        EmbedBuilder builder = baseEmbed("BOT全体の累計読み上げ統計");
        builder.addField("累計文字数", charSum + "文字", true);
        builder.addField("累計メッセージ数", messageSum + "件", true);
        addUptimeFields(builder);

        event.replyEmbeds(builder.build()).setEphemeral(true).queue();
    }

    private void server(SlashCommandInteractionEvent event) {
        Guild guild = Objects.requireNonNull(event.getGuild());
        DataRepository repo = SaveDataManager.getInstance().getRepository();
        long botId = event.getJDA().getSelfUser().getIdLong();
        long serverId = guild.getIdLong();

        long charSum = repo.sumServerAllCharCount(botId, serverId);
        long messageSum = repo.sumServerAllMessageCount(botId, serverId);

        EmbedBuilder builder = baseEmbed("このサーバーの累計読み上げ統計");
        builder.addField("サーバー名", guild.getName(), false);
        builder.addField("累計文字数", charSum + "文字", true);
        builder.addField("累計メッセージ数", messageSum + "件", true);
        addUptimeFields(builder);

        event.replyEmbeds(builder.build()).setEphemeral(true).queue();
    }

    private EmbedBuilder baseEmbed(String title) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(getConfigManager().getConfig().getThemeColor());
        builder.setTitle(title);
        return builder;
    }

    private void addUptimeFields(EmbedBuilder builder) {
        long startup = getITTSRuntime().getStartupTime();
        long now = System.currentTimeMillis();
        Duration uptime = Duration.ofMillis(now - startup);
        builder.addField("稼働開始", String.format(RELATIVE_TIME_FORMAT, startup / 1000), true);
        builder.addField("稼働時間", formatDuration(uptime), true);
    }

    private String formatDuration(Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        if (days > 0) {
            return days + "日" + hours + "時間" + minutes + "分";
        }
        if (hours > 0) {
            return hours + "時間" + minutes + "分" + seconds + "秒";
        }
        return minutes + "分" + seconds + "秒";
    }
}
