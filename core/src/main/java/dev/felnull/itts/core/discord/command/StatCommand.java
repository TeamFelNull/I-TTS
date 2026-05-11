package dev.felnull.itts.core.discord.command;

import dev.felnull.itts.core.statistics.StatisticsManager;
import dev.felnull.itts.core.statistics.dao.StatisticsDAO.TTSCountSum;
import dev.felnull.itts.core.statistics.repository.StatisticsRepository;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Objects;

/**
 * 読み上げ統計表示コマンド
 */
public class StatCommand extends BaseCommand {

    /**
     * コンストラクタ
     */
    public StatCommand() {
        super("stat");
    }

    @NotNull
    @Override
    public SlashCommandData createSlashCommand() {
        return Commands.slash("stat", "現在のサーバーの累計読み上げ統計を表示")
                .setContexts(InteractionContextType.GUILD)
                .setDefaultPermissions(OWNERS_PERMISSIONS);
    }

    @Override
    public void commandInteraction(SlashCommandInteractionEvent event) {
        StatisticsRepository repo = StatisticsManager.getInstance().getRepository();
        if (repo == null) {
            event.reply("統計機能は無効になっています").setEphemeral(true).queue();
            return;
        }

        Guild guild = Objects.requireNonNull(event.getGuild());
        long botId = getBot().getBotId();
        long serverId = guild.getIdLong();

        TTSCountSum sum = repo.sumCount(botId, serverId, null, null);

        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(getConfigManager().getConfig().getThemeColor());
        builder.setTitle("このサーバーの累計読み上げ統計");
        builder.addField("サーバー名", guild.getName(), false);
        builder.addField("累計文字数", sum.charCount() + "文字", true);
        builder.addField("累計メッセージ数", sum.messageCount() + "件", true);
        addUptimeFields(builder);

        event.replyEmbeds(builder.build()).setEphemeral(true).queue();
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
