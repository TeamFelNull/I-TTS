package dev.felnull.itts.core.discord.command;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * 再接続コマンド
 *
 * @author MORIMORI0317
 */
public class ReconnectCommand extends BaseCommand {

    /**
     * コンストラクタ
     */
    public ReconnectCommand() {
        super("reconnect");
    }

    @NotNull
    @Override
    public SlashCommandData createSlashCommand() {
        return Commands.slash("reconnect", "読み上げBOTをVCに再接続")
                .setContexts(InteractionContextType.GUILD)
                .setDefaultPermissions(MEMBERS_PERMISSIONS);
    }

    @Override
    public void commandInteraction(SlashCommandInteractionEvent event) {
        Guild guild = Objects.requireNonNull(event.getGuild());
        AudioManager audioManager = Objects.requireNonNull(guild.getAudioManager());

        if (audioManager.isConnected()) {
            AudioChannelUnion connectedChannel = Objects.requireNonNull(audioManager.getConnectedChannel());
            event.reply(connectedChannel.getAsMention() + "に再接続します。").queue();

            audioManager.closeAudioConnection();

            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }

                getTTSManager().setReadAroundChannel(event.getGuild(), event.getChannel());
                audioManager.openAudioConnection(connectedChannel);
            }, getAsyncExecutor());
        } else {
            event.reply("現在VCに接続していません。").setEphemeral(true).queue();
        }
    }
}
