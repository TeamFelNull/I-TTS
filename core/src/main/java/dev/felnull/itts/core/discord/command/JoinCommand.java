package dev.felnull.itts.core.discord.command;

import dev.felnull.itts.core.discord.AutoDisconnectMode;
import dev.felnull.itts.core.discord.ConnectControl;
import dev.felnull.itts.core.savedata.SaveDataManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * 参加コマンド
 *
 * @author MORIMORI0317
 */
public class JoinCommand extends BaseCommand {

    /**
     * コンストラクタ
     */
    public JoinCommand() {
        super("join");
    }

    @NotNull
    @Override
    public SlashCommandData createSlashCommand() {
        return Commands.slash("join", "読み上げBOTをVCに呼び出す")
                .addOptions(new OptionData(OptionType.CHANNEL, "channel", "チャンネル指定")
                        .setChannelTypes(ChannelType.VOICE, ChannelType.STAGE))
                .setContexts(InteractionContextType.GUILD)
                .setDefaultPermissions(MEMBERS_PERMISSIONS);
    }

    @Override
    public void commandInteraction(SlashCommandInteractionEvent event) {
        Guild guild = Objects.requireNonNull(event.getGuild());
        Member member = Objects.requireNonNull(event.getMember());

        OptionMapping interactionChannel = event.getInteraction().getOption("channel");
        AudioChannel joinTargetChannel = null;

        if (interactionChannel != null) {
            joinTargetChannel = interactionChannel.getAsChannel().asAudioChannel();
        } else {
            GuildVoiceState vs = member.getVoiceState();

            if (vs != null) {
                joinTargetChannel = vs.getChannel();
            }
        }

        if (joinTargetChannel == null) {
            event.reply("VCに参加している状態で使用するか、チャンネルを指定する必要があります。").setEphemeral(true).queue();
            return;
        }

        AudioManager audioManager = guild.getAudioManager();
        if (audioManager.isConnected() && audioManager.getConnectedChannel() != null && audioManager.getConnectedChannel().getIdLong() == joinTargetChannel.getIdLong()) {
            event.reply("すでに接続しています").setEphemeral(true).queue();
            return;
        }

        getTTSManager().setReadAroundChannel(guild, event.getChannel());

        try {
            audioManager.openAudioConnection(joinTargetChannel);
        } catch (InsufficientPermissionException ex) {
            if (ex.getPermission() == Permission.VOICE_CONNECT) {

                event.reply(joinTargetChannel.getAsMention() + "に接続する権限がありません。").setEphemeral(true).queue();
            } else {
                event.reply(joinTargetChannel.getAsMention() + "接続に失敗しました。").setEphemeral(true).queue();
            }
            return;
        }

        boolean autoDisFlg = false;

        AutoDisconnectMode autoDisMode = SaveDataManager.getInstance().getRepository()
                .getServerData(guild.getIdLong())
                .getAutoDisconnectMode();
        if (autoDisMode.isOn() && ConnectControl.isNoUser(joinTargetChannel)) {
            autoDisFlg = true;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(joinTargetChannel.getAsMention()).append("に接続しました。\n");

        if (autoDisFlg) {
            sb.append("誰もチャンネルに参加しない場合は自動的に切断します。");
        }

        event.reply(sb.toString()).queue();
    }
}
