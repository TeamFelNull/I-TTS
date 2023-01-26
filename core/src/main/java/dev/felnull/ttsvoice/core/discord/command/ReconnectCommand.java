package dev.felnull.ttsvoice.core.discord.command;

import dev.felnull.ttsvoice.core.util.DiscordUtils;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ReconnectCommand extends BaseCommand {
    public ReconnectCommand() {
        super("reconnect");
    }

    @NotNull
    @Override
    public SlashCommandData createSlashCommand() {
        return Commands.slash("reconnect", "読み上げBOTをVCに再接続")
                .setGuildOnly(true)
                .setDefaultPermissions(MEMBERS_PERMISSIONS);
    }

    @Override
    public void commandInteraction(SlashCommandInteractionEvent event) {
        var audioManager = event.getGuild().getAudioManager();

        if (audioManager.isConnected()) {
            var connectedChannel = audioManager.getConnectedChannel();
            event.reply(DiscordUtils.createChannelMention(connectedChannel) + "に再接続します。").queue();

            audioManager.closeAudioConnection();

            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ignored) {
                }

                getRuntime().getTTSManager().setReadAroundChannel(event.getGuild(), event.getChannel());
                audioManager.openAudioConnection(connectedChannel.asVoiceChannel());
            }, getRuntime().getAsyncWorkerExecutor());
        } else {
            event.reply("現在VCに接続していません。").setEphemeral(true).queue();
        }
    }
}
