package dev.felnull.ttsvoice.core.discord.command;

import dev.felnull.ttsvoice.core.TTSVoiceRuntime;
import dev.felnull.ttsvoice.core.util.DiscordUtils;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

public class LeaveCommand extends BaseCommand {
    public LeaveCommand(@NotNull TTSVoiceRuntime runtime) {
        super(runtime, "leave");
    }

    @NotNull
    @Override
    public SlashCommandData createSlashCommand() {
        return Commands.slash("leave", "読み上げBOTをVCから切断")
                .setGuildOnly(true)
                .setDefaultPermissions(MEMBERS_PERMISSIONS);
    }

    @Override
    public void commandInteraction(SlashCommandInteractionEvent event) {
        var audioManager = event.getGuild().getAudioManager();

        if (audioManager.isConnected()) {
            var connectedChannel = audioManager.getConnectedChannel();
            event.reply(DiscordUtils.createChannelMention(connectedChannel) + "から切断します。").queue();

            audioManager.closeAudioConnection();
        } else {
            event.reply("現在VCに接続していません。").setEphemeral(true).queue();
        }
    }
}
