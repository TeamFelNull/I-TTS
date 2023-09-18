package dev.felnull.itts.core.discord.command;

import dev.felnull.itts.core.tts.TTSInstance;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * スキップコマンド
 *
 * @author MORIMORI0317
 */
public class SkipCommand extends BaseCommand {

    /**
     * コンストラクタ
     */
    public SkipCommand() {
        super("skip");
    }

    @Override
    public @NotNull SlashCommandData createSlashCommand() {
        return Commands.slash("skip", "現在の読み上げキューを飛ばす")
                .setGuildOnly(true)
                .setDefaultPermissions(OWNERS_PERMISSIONS);
    }

    @Override
    public void commandInteraction(SlashCommandInteractionEvent event) {
        Guild guild = Objects.requireNonNull(event.getGuild());

        TTSInstance ttsInstance = getTTSManager().getTTSInstance(guild.getIdLong());

        if (ttsInstance == null) {
            event.reply("現在VCに接続していません。").setEphemeral(true).queue();
            return;
        }

        int skipCt = ttsInstance.skipAll();

        if (skipCt >= 1) {
            event.reply(skipCt + "個の読み上げをスキップしました。").queue();
        } else {
            event.reply("現在読み上げていません。").setEphemeral(true).queue();
        }
    }
}
