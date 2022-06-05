package dev.felnull.ttsvoice.tts;

import dev.felnull.ttsvoice.Main;
import dev.felnull.ttsvoice.util.DiscordUtil;
import dev.felnull.ttsvoice.util.TextUtil;
import dev.felnull.ttsvoice.voice.inm.INMEntry;
import dev.felnull.ttsvoice.voice.inm.INMManager;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TTSListener extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent e) {
        if ("join".equals(e.getName())) {
            if (!checkNeedAdmin(e.getMember(), e))
                return;
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
            if (!checkNeedAdmin(e.getMember(), e))
                return;
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
            if (!checkNeedAdmin(e.getMember(), e))
                return;
            var audioManager = e.getGuild().getAudioManager();
            if (audioManager.isConnected()) {
                var chn = audioManager.getConnectedChannel();
                e.reply(DiscordUtil.createChannelMention(chn) + "に再接続します").queue();
                audioManager.closeAudioConnection();
                TTSManager.getInstance().removeTTSChanel(e.getGuild().getIdLong());
                var rt = new ReconnectThread(audioManager, e.getGuild(), chn, e.getChannel());
                rt.start();
            } else {
                e.reply("現在VCに接続していません").queue();
            }
        } else if ("voice".equals(e.getName()) && "list".equals(e.getSubcommandName())) {
            var msg = new MessageBuilder().append("読み上げ音声タイプ一覧\n");
            StringBuilder sb = new StringBuilder();
            for (IVoiceType voiceType : TTSManager.getInstance().getVoiceTypes(e.getUser().getIdLong(), e.getGuild().getIdLong())) {
                sb.append(voiceType.getId()).append(" ").append(voiceType.getTitle()).append("\n");
            }
            msg.appendCodeLine(sb.toString());
            e.reply(msg.build()).setEphemeral(true).queue();
        } else if ("voice".equals(e.getName()) && "change".equals(e.getSubcommandName())) {
            var uop = e.getOption("user");
            if (uop != null && DiscordUtil.hasPermission(e.getMember())) {
                e.reply("他ユーザーを編集するための権限がありません").queue();
                return;
            }
            User user = uop == null ? e.getUser() : uop.getAsUser();

            if (user.isBot()) {
                e.reply(DiscordUtil.getName(e.getGuild(), user) + "はBOTです").queue();
                return;
            }

            var op = e.getOption("voice_type");
            String id = op == null ? null : op.getAsString();
            var type = TTSManager.getInstance().getVoiceTypeById(id, e.getUser().getIdLong(), e.getGuild().getIdLong());
            if (type == null) {
                e.reply("存在しない読み上げタイプです").queue();
                return;
            }
            var pre = TTSManager.getInstance().getUserVoiceType(user.getIdLong(), e.getGuild().getIdLong());
            if (pre == type) {
                e.reply("読み上げ音声タイプは変更されませんでした").queue();
                return;
            }

            TTSManager.getInstance().setUserVoceTypes(user.getIdLong(), type);
            e.reply(DiscordUtil.getName(e.getGuild(), user) + "の読み上げ音声タイプを[" + type.getTitle() + "]に変更しました").queue();
        } else if ("voice".equals(e.getName()) && "check".equals(e.getSubcommandName())) {
            var uop = e.getOption("user");
            if (uop != null && DiscordUtil.hasPermission(e.getMember())) {
                e.reply("他ユーザーを確認するための権限がありません").queue();
                return;
            }
            User user = uop == null ? e.getUser() : uop.getAsUser();
            if (user.isBot()) {
                e.reply(DiscordUtil.getName(e.getGuild(), user) + "はBOTです").queue();
                return;
            }
            var type = TTSManager.getInstance().getUserVoiceType(user.getIdLong(), e.getGuild().getIdLong());
            e.reply(DiscordUtil.getName(e.getGuild(), user) + "の現在の読み上げタイプは[" + type.getTitle() + "]です").setEphemeral(true).queue();
        } else if ("deny".equals(e.getName()) && "list".equals(e.getSubcommandName())) {
            if (!DiscordUtil.hasPermission(e.getMember())) {
                e.reply("読み上げ拒否をされているユーザ一覧を見る権限がありません").setEphemeral(true).queue();
                return;
            }
            var lst = Main.SAVE_DATA.getDenyUsers(e.getGuild().getIdLong());
            if (lst.isEmpty()) {
                e.reply("読み上げ拒否されたユーザは存在しません").setEphemeral(true).queue();
                return;
            }

            var msg = new MessageBuilder().append("読み上げ拒否されたユーザ一覧\n");
            StringBuilder sb = new StringBuilder();
            for (Long deny : lst) {
                sb.append(DiscordUtil.getName(e.getGuild(), Main.JDA.getUserById(deny))).append("\n");
            }
            msg.appendCodeLine(sb.toString());
            e.reply(msg.build()).setEphemeral(true).queue();
        } else if ("deny".equals(e.getName()) && "add".equals(e.getSubcommandName())) {
            if (!checkNeedAdmin(e.getMember(), e))
                return;
            if (!DiscordUtil.hasPermission(e.getMember())) {
                e.reply("ユーザを読み上げ拒否する権限がありません").setEphemeral(true).queue();
                return;
            }
            var uop = e.getOption("user");
            if (uop == null) {
                e.reply("ユーザを指定してください").setEphemeral(true).queue();
                return;
            }
            if (uop.getAsUser().isBot()) {
                e.reply(DiscordUtil.getName(e.getGuild(), uop.getAsUser()) + "はBOTです").setEphemeral(true).queue();
                return;
            }

            if (Main.SAVE_DATA.isDenyUser(e.getGuild().getIdLong(), uop.getAsUser().getIdLong())) {
                e.reply("すでに読み上げ拒否をされているユーザです").setEphemeral(true).queue();
                return;
            }
            Main.SAVE_DATA.addDenyUser(e.getGuild().getIdLong(), uop.getAsUser().getIdLong());
            e.reply(DiscordUtil.getName(e.getGuild(), uop.getAsUser()) + "の読み上げ拒否します").setEphemeral(true).queue();
        } else if ("deny".equals(e.getName()) && "remove".equals(e.getSubcommandName())) {
            if (!checkNeedAdmin(e.getMember(), e))
                return;
            if (!DiscordUtil.hasPermission(e.getMember())) {
                e.reply("ユーザの読み上げ拒否を解除する権限がありません").setEphemeral(true).queue();
                return;
            }
            var uop = e.getOption("user");
            if (uop == null) {
                e.reply("ユーザを指定してください").setEphemeral(true).queue();
                return;
            }
            if (uop.getAsUser().isBot()) {
                e.reply(DiscordUtil.getName(e.getGuild(), uop.getAsUser()) + "はBOTです").setEphemeral(true).queue();
                return;
            }
            if (!Main.SAVE_DATA.isDenyUser(e.getGuild().getIdLong(), uop.getAsUser().getIdLong())) {
                e.reply("読み上げ拒否をされていないユーザです").setEphemeral(true).queue();
                return;
            }
            Main.SAVE_DATA.removeDenyUser(e.getGuild().getIdLong(), uop.getAsUser().getIdLong());
            e.reply(DiscordUtil.getName(e.getGuild(), uop.getAsUser()) + "の読み上げ拒否を解除します").setEphemeral(true).queue();
        } else if ("inm".equals(e.getName())) {
            var op = e.getOption("search");
            if (op != null && TTSManager.getInstance().getVoiceTypes(e.getUser().getIdLong(), e.getGuild().getIdLong()).contains(INMManager.getInstance().getVoice())) {
                TTSManager.getInstance().onText(e.getGuild().getIdLong(), INMManager.getInstance().getVoice(), op.getAsString());
            }
            e.deferReply().queue();
            e.getHook().deleteOriginal().queue();
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent e) {
        if ("voice".equals(e.getName()) && "change".equals(e.getSubcommandName())) {
            var op = e.getInteraction().getOption("voice_type");
            String str = op == null ? null : op.getAsString();

            List<IVoiceType> choices = new ArrayList<>();

            for (IVoiceType voiceType : TTSManager.getInstance().getVoiceTypes(e.getUser().getIdLong(), e.getGuild().getIdLong())) {
                if (str != null && (voiceType.getId().contains(str) || voiceType.getTitle().contains(str)))
                    choices.add(voiceType);
            }

            if (choices.isEmpty()) {
                choices.addAll(TTSManager.getInstance().getVoiceTypes(e.getUser().getIdLong(), e.getGuild().getIdLong()));
            }

            choices = choices.stream().sorted(Comparator.comparingInt(n -> {
                if (str == null) return 0;
                int i = TextUtil.getComplementPoint(n.getId(), str) * 2;
                int t = TextUtil.getComplementPoint(n.getTitle(), str);
                return i + t;
            })).toList();

            if (choices.size() > 25) {
                List<IVoiceType> nc = new ArrayList<>();
                int ct = 0;
                for (IVoiceType choice : choices) {
                    nc.add(choice);
                    ct++;
                    if (ct >= 25)
                        break;
                }
                choices = nc;
            }

            e.replyChoices(choices.stream().map(n -> new Command.Choice(n.getTitle(), n.getId())).toList()).queue();
        } else if ("inm".equals(e.getName())) {
            var op = e.getInteraction().getOption("search");
            List<INMEntry> entries = new ArrayList<>();
            if (op != null && TTSManager.getInstance().getVoiceTypes(e.getUser().getIdLong(), e.getGuild().getIdLong()).contains(INMManager.getInstance().getVoice())) {
                var im = INMManager.getInstance();
                try {
                    var scr = im.search(op.getAsString(), 25);
                    scr = im.sort(scr);
                    entries.addAll(scr);
                } catch (Exception ignored) {
                }
            }
            if (entries.size() > 25) {
                List<INMEntry> nc = new ArrayList<>();
                int ct = 0;
                for (INMEntry entry : entries) {
                    nc.add(entry);
                    ct++;
                    if (ct >= 25)
                        break;
                }
                entries = nc;
            }
            e.replyChoices(entries.stream().map(n -> new Command.Choice(n.name(), n.name())).toList()).queue();
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        var tm = TTSManager.getInstance();
        if (tm.getTTSChanel(e.getGuild().getIdLong()) == e.getChannel().getIdLong() && !e.getMember().getUser().isBot()) {
            tm.onChat(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong(), e.getMessage().getContentRaw());
            for (Message.Attachment attachment : e.getMessage().getAttachments()) {
                if (!attachment.isImage() && !attachment.isVideo())
                    tm.onText(e.getGuild().getIdLong(), tm.getUserVoiceType(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong()), attachment.getFileName());
            }
        }
    }

    private boolean checkNeedAdmin(Member member, IReplyCallback callback) {
        if (!DiscordUtil.hasNeedAdminPermission(member)) {
            callback.reply("コマンドを実行する権限がありません").setEphemeral(true).queue();
            return false;
        }
        return true;
    }

    private static class ReconnectThread extends Thread {
        private final AudioManager manager;
        private final Guild guild;
        private final AudioChannel channel;
        private final Channel textChannel;

        private ReconnectThread(AudioManager manager, Guild guild, AudioChannel channel, Channel textChannel) {
            this.manager = manager;
            this.guild = guild;
            this.channel = channel;
            this.textChannel = textChannel;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
            this.manager.openAudioConnection(this.channel);
            TTSManager.getInstance().setTTSChanel(guild.getIdLong(), textChannel.getIdLong());
        }
    }
}
