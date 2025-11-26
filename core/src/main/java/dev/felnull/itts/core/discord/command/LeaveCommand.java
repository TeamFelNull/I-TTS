package dev.felnull.itts.core.discord.command;

import dev.felnull.itts.core.savedata.SaveDataManager;
import dev.felnull.itts.core.savedata.repository.BotStateData;
import dev.felnull.itts.core.savedata.repository.DataRepository;
import dev.felnull.itts.core.tts.TTSChannelPair;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
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
                .setContexts(InteractionContextType.GUILD)
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

            DataRepository repo = SaveDataManager.getInstance().getRepository();
            BotStateData botStateData = repo.getBotStateData(guild.getIdLong(), getBot().getBotId());
            TTSChannelPair reconnectChannelPair = botStateData.getReconnectChannelPair();

            if (reconnectChannelPair != null) {
                AudioChannel audioChannel = event.getJDA().getVoiceChannelById(reconnectChannelPair.speakAudioChannel());
                if (audioChannel != null) {
                    event.reply(audioChannel.getAsMention() + "から切断します。").queue();
                    botStateData.setReconnectChannelPair(null);
                    reconnectFlg = true;
                }
            }

            if (!reconnectFlg) {
                event.reply("現在VCに接続していません。").setEphemeral(true).queue();
            }
        }
    }
}
