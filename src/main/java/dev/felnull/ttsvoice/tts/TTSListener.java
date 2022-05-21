package dev.felnull.ttsvoice.tts;

import dev.felnull.ttsvoice.util.DiscordUtil;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TTSListener extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent e) {
        if ("join".equals(e.getName())) {
            var channel = e.getInteraction().getOption("channel");
            AudioChannel audioChannel;
            if (channel != null) {
                audioChannel = channel.getAsAudioChannel();
            } else {
                audioChannel = e.getMember().getVoiceState().getChannel();
                if (audioChannel == null) {
                    e.reply("VCに入ってる状態で使用するか、チャンネルを指定してください。").queue();
                    return;
                }
            }

            if (audioChannel == null) {
                e.reply("チャンネルを取得できませんでした。").queue();
                return;
            }

            var audioManager = e.getGuild().getAudioManager();
            if (audioManager.isConnected() && audioManager.getConnectedChannel() != null && audioManager.getConnectedChannel().getIdLong() == audioChannel.getIdLong()) {
                e.reply("すでに接続しています").queue();
                return;
            }

            audioManager.openAudioConnection(audioChannel);
            TTSManager.getInstance().setTTSChanel(e.getGuild().getIdLong(), e.getChannel().getIdLong());

            e.reply(DiscordUtil.createChannelMention(audioChannel) + "に接続しました").queue();
        } else if ("leave".equals(e.getName())) {
            var audioManager = e.getGuild().getAudioManager();
            if (audioManager.isConnected()) {
                var chn = audioManager.getConnectedChannel();
                e.reply(DiscordUtil.createChannelMention(chn) + "から切断します").queue();
                audioManager.closeAudioConnection();
                TTSManager.getInstance().removeTTSChanel(e.getGuild().getIdLong());
            } else {
                e.reply("現在VCに接続していません").queue();
            }
        } else if ("reconnect".equals(e.getName())) {
            var audioManager = e.getGuild().getAudioManager();
            if (audioManager.isConnected()) {
                var chn = audioManager.getConnectedChannel();
                e.reply(DiscordUtil.createChannelMention(chn) + "に再接続します").queue();
                audioManager.closeAudioConnection();
                TTSManager.getInstance().removeTTSChanel(e.getGuild().getIdLong());
                var rt = new ReconnectThread(audioManager, e.getGuild(), chn);
                rt.start();
            } else {
                e.reply("現在VCに接続していません").queue();
            }
        } else if ("voice".equals(e.getName()) && "list".equals(e.getSubcommandName())) {
            var msg = new MessageBuilder().append("読み上げ音声タイプ一覧\n");
            StringBuilder sb = new StringBuilder();
            for (IVoiceType voiceType : TTSManager.getInstance().getVoiceTypes()) {
                sb.append(voiceType.getId()).append(" ").append(voiceType.getTitle()).append("\n");
            }
            msg.appendCodeLine(sb.toString());
            e.reply(msg.build()).queue();
        } else if ("voice".equals(e.getName()) && "change".equals(e.getSubcommandName())) {
            var uop = e.getOption("user");
            if (uop != null && DiscordUtil.canEdit(e.getMember().getRoles())) {
                e.reply("他ユーザーを編集するための権限がありません").queue();
                return;
            }
            User user = uop == null ? e.getUser() : uop.getAsUser();

            if (user.isBot()) {
                e.reply(e.getGuild().getMember(user).getEffectiveName() + "はBOTです").queue();
                return;
            }

            var op = e.getOption("voice_type");
            String id = op == null ? null : op.getAsString();
            var type = TTSManager.getInstance().getVoiceTypeById(id);
            if (type == null) {
                e.reply("存在しない読み上げタイプです").queue();
                return;
            }
            var pre = TTSManager.getInstance().getUserVoiceType(user.getIdLong());
            if (pre == type) {
                e.reply("読み上げ音声タイプは変更されませんでした").queue();
                return;
            }

            TTSManager.getInstance().setUserVoceTypes(user.getIdLong(), type);
            e.reply(e.getGuild().getMember(user).getEffectiveName() + "の読み上げ音声タイプを[" + type.getTitle() + "]に変更しました").queue();
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent e) {
        if ("voice".equals(e.getName()) && "change".equals(e.getSubcommandName())) {
            var op = e.getInteraction().getOption("voice_type");
            String str = op == null ? null : op.getAsString();

            List<IVoiceType> choices = new ArrayList<>();

            for (IVoiceType voiceType : TTSManager.getInstance().getVoiceTypes()) {
                if (str != null && (voiceType.getId().contains(str) || voiceType.getTitle().contains(str)))
                    choices.add(voiceType);
            }

            if (choices.isEmpty()) {
                choices.addAll(TTSManager.getInstance().getVoiceTypes());
            }

            e.replyChoices(choices.stream().map(n -> new Command.Choice(n.getTitle(), n.getId())).toList()).queue();
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        var tm = TTSManager.getInstance();
        if (tm.getTTSChanel(e.getGuild().getIdLong()) == e.getChannel().getIdLong() && !e.getMember().getUser().isBot()) {
            tm.onChat(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong(), e.getMessage().getContentRaw());
            for (Message.Attachment attachment : e.getMessage().getAttachments()) {
                tm.onText(e.getGuild().getIdLong(), tm.getUserVoiceType(e.getMember().getUser().getIdLong()), attachment.getFileName());
            }
        }
    }

    private static class ReconnectThread extends Thread {
        private final AudioManager manager;
        private final Guild guild;
        private final AudioChannel channel;

        private ReconnectThread(AudioManager manager, Guild guild, AudioChannel channel) {
            this.manager = manager;
            this.guild = guild;
            this.channel = channel;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
            this.manager.openAudioConnection(this.channel);
            TTSManager.getInstance().setTTSChanel(guild.getIdLong(), channel.getIdLong());
        }
    }
}
