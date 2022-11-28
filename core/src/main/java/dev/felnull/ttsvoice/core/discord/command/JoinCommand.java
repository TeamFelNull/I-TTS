package dev.felnull.ttsvoice.core.discord.command;

import dev.felnull.ttsvoice.core.TTSVoiceRuntime;
import dev.felnull.ttsvoice.core.util.DiscordUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

public class JoinCommand extends BaseCommand {
    public JoinCommand(@NotNull TTSVoiceRuntime runtime) {
        super(runtime, "join");
    }

    @NotNull
    @Override
    public SlashCommandData createSlashCommand() {
        return Commands.slash("join", "読み上げBOTをVCに呼び出す")
                .addOptions(new OptionData(OptionType.CHANNEL, "channel", "チャンネル指定")
                        .setChannelTypes(ChannelType.VOICE, ChannelType.STAGE))
                .setGuildOnly(true)
                .setDefaultPermissions(MEMBERS_PERMISSIONS);
    }

    @Override
    public void commandInteraction(SlashCommandInteractionEvent event) {
        var interactionChannel = event.getInteraction().getOption("channel");
        AudioChannel joinTargetChannel = null;

        if (interactionChannel != null) {
            joinTargetChannel = interactionChannel.getAsChannel().asAudioChannel();
        } else {
            var member = event.getMember();
            var vs = member.getVoiceState();
            if (vs != null)
                joinTargetChannel = vs.getChannel();

        }

        if (joinTargetChannel == null) {
            event.reply("VCに参加している状態で使用するか、チャンネルを指定する必要があります。").setEphemeral(true).queue();
            return;
        }

        var audioManager = event.getGuild().getAudioManager();
        if (audioManager.isConnected() && audioManager.getConnectedChannel() != null && audioManager.getConnectedChannel().getIdLong() == joinTargetChannel.getIdLong()) {
            event.reply("すでに接続しています").setEphemeral(true).queue();
            return;
        }

        try {
            audioManager.openAudioConnection(joinTargetChannel);
        } catch (InsufficientPermissionException ex) {
            if (ex.getPermission() == Permission.VOICE_CONNECT) {
                event.reply(DiscordUtils.createChannelMention(joinTargetChannel) + "に接続する権限がありません。").setEphemeral(true).queue();
            } else {
                event.reply(DiscordUtils.createChannelMention(joinTargetChannel) + "接続に失敗しました。").setEphemeral(true).queue();
            }
            return;
        }

        event.reply(DiscordUtils.createChannelMention(joinTargetChannel) + "に接続しました。").queue();
    }
}
