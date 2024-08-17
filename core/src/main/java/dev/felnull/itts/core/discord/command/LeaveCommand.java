package dev.felnull.itts.core.discord.command;

import dev.felnull.itts.core.savedata.KariAutoDisconnectData;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * 退出コマンド
 *
 * @author MORIMORI0317
 */
public class LeaveCommand extends BaseCommand {

    /**
     * コンストラクタ
     */
    public LeaveCommand() {
        super("leave");
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
        Guild guild = Objects.requireNonNull(event.getGuild());
        AudioManager audioManager = guild.getAudioManager();

        if (audioManager.isConnected()) {
            AudioChannelUnion connectedChannel = audioManager.getConnectedChannel();

            if (connectedChannel != null) {
                event.reply(connectedChannel.getAsMention() + "から切断します。").queue();
                audioManager.closeAudioConnection();
            } else {
                event.reply("切断できませんでした。").queue();
            }

        } else {
            boolean reconnectFlg = false;

            KariAutoDisconnectData.TTSChannelPair ttsChannelPair = KariAutoDisconnectData.getReconnectChannel(guild.getIdLong());
            if (ttsChannelPair != null && ttsChannelPair.speakAudioChannel() != -1 && ttsChannelPair.readAroundTextChannel() != -1) {
                AudioChannel audioChannel = event.getJDA().getVoiceChannelById(ttsChannelPair.speakAudioChannel());
                if (audioChannel != null) {
                    event.reply(audioChannel.getAsMention() + "から切断します。").queue();
                    KariAutoDisconnectData.setReconnectChannel(guild.getIdLong(), new KariAutoDisconnectData.TTSChannelPair(-1, -1));
                    reconnectFlg = true;
                }
            }

            if (!reconnectFlg) {
                event.reply("現在VCに接続していません。").setEphemeral(true).queue();
            }
        }
    }
}
