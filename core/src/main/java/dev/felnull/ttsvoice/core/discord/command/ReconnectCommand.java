package dev.felnull.ttsvoice.core.discord.command;

import dev.felnull.ttsvoice.core.TTSVoiceRuntime;
import dev.felnull.ttsvoice.core.util.DiscordUtils;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ReconnectCommand extends BaseCommand {
    public ReconnectCommand(@NotNull TTSVoiceRuntime runtime) {
        super(runtime, "reconnect");
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

                runtime.getTTSManager().setReadAroundChannel(event.getGuild(), event.getChannel().getIdLong());
                audioManager.openAudioConnection(connectedChannel.asVoiceChannel());
            }, runtime.getAsyncWorkerExecutor());
        } else {
            event.reply("現在VCに接続していません。").setEphemeral(true).queue();
        }
    }
}
