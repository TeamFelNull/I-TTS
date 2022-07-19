package dev.felnull.ttsvoice.tts;

import dev.felnull.fnjl.tuple.FNPair;
import dev.felnull.ttsvoice.Main;
import dev.felnull.ttsvoice.tts.sayvoice.VCEventSayVoice;
import dev.felnull.ttsvoice.util.DiscordUtils;
import dev.felnull.ttsvoice.util.TextUtils;
import dev.felnull.ttsvoice.voice.VoiceType;
import dev.felnull.ttsvoice.voice.inm.INMEntry;
import dev.felnull.ttsvoice.voice.inm.INMManager;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class TTSListener extends ListenerAdapter {
    private static final Random rand = new Random();
    private final int botNumber;

    public TTSListener(int botNumber) {
        this.botNumber = botNumber;
    }

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

            try {
                audioManager.openAudioConnection(audioChannel);
            } catch (InsufficientPermissionException ex) {
                if (ex.getPermission() == Permission.VOICE_CONNECT) {
                    e.reply(DiscordUtils.createChannelMention(audioChannel) + "に接続する権限がありません").setEphemeral(true).queue();
                } else {
                    e.reply(DiscordUtils.createChannelMention(audioChannel) + "接続に失敗しました").setEphemeral(true).queue();
                }
                return;
            }

            TTSManager.getInstance().connect(new BotAndGuild(botNumber, e.getGuild().getIdLong()), e.getChannel().getIdLong(), audioChannel.getIdLong());

            e.reply(DiscordUtils.createChannelMention(audioChannel) + "に接続しました").queue();
        } else if ("leave".equals(e.getName())) {
            if (!checkNeedAdmin(e.getMember(), e))
                return;
            var audioManager = e.getGuild().getAudioManager();
            if (audioManager.isConnected()) {
                var chn = audioManager.getConnectedChannel();
                e.reply(DiscordUtils.createChannelMention(chn) + "から切断します").queue();
                audioManager.closeAudioConnection();
                TTSManager.getInstance().disconnect(new BotAndGuild(botNumber, e.getGuild().getIdLong()));
            } else {
                e.reply("現在VCに接続していません").queue();
            }
        } else if ("reconnect".equals(e.getName())) {
            if (!checkNeedAdmin(e.getMember(), e))
                return;
            var audioManager = e.getGuild().getAudioManager();
            if (audioManager.isConnected()) {
                var chn = audioManager.getConnectedChannel();
                e.reply(DiscordUtils.createChannelMention(chn) + "に再接続します").queue();
                audioManager.closeAudioConnection();
                TTSManager.getInstance().disconnect(new BotAndGuild(botNumber, e.getGuild().getIdLong()));
                var rt = new ReconnectThread(audioManager, e.getGuild(), chn, e.getChannel());
                rt.start();
            } else {
                e.reply("現在VCに接続していません").queue();
            }
        } else if ("voice".equals(e.getName()) && "list".equals(e.getSubcommandName())) {
            var msg = new MessageBuilder().append("読み上げ音声タイプ一覧\n");
            StringBuilder sb = new StringBuilder();
            for (VoiceType voiceType : TTSManager.getInstance().getVoiceTypes(e.getUser().getIdLong(), e.getGuild().getIdLong())) {
                sb.append(voiceType.getId()).append(" ").append(voiceType.getTitle()).append("\n");
            }
            msg.appendCodeLine(sb.toString());
            e.reply(msg.build()).setEphemeral(true).queue();
        } else if ("voice".equals(e.getName()) && "change".equals(e.getSubcommandName())) {
            var uop = e.getOption("user");
            if (uop != null && DiscordUtils.hasPermission(e.getMember())) {
                e.reply("他ユーザーを編集するための権限がありません").queue();
                return;
            }
            User user = uop == null ? e.getUser() : uop.getAsUser();

            if (user.isBot()) {
                e.reply(DiscordUtils.getName(botNumber, e.getGuild(), user, user.getIdLong()) + "はBOTです").queue();
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
            e.reply(DiscordUtils.getName(botNumber, e.getGuild(), user, user.getIdLong()) + "の読み上げ音声タイプを[" + type.getTitle() + "]に変更しました").queue();
        } else if ("voice".equals(e.getName()) && "check".equals(e.getSubcommandName())) {
            var uop = e.getOption("user");
            if (uop != null && DiscordUtils.hasPermission(e.getMember())) {
                e.reply("他ユーザーを確認するための権限がありません").queue();
                return;
            }
            User user = uop == null ? e.getUser() : uop.getAsUser();
            if (user.isBot()) {
                e.reply(DiscordUtils.getName(botNumber, e.getGuild(), user, user.getIdLong()) + "はBOTです").queue();
                return;
            }
            var type = TTSManager.getInstance().getUserVoiceType(user.getIdLong(), e.getGuild().getIdLong());
            e.reply(DiscordUtils.getName(botNumber, e.getGuild(), user, user.getIdLong()) + "の現在の読み上げタイプは[" + type.getTitle() + "]です").setEphemeral(true).queue();
        } else if ("deny".equals(e.getName()) && "list".equals(e.getSubcommandName())) {
            if (!DiscordUtils.hasPermission(e.getMember())) {
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
                sb.append(DiscordUtils.getName(botNumber, e.getGuild(), Main.getJDA(botNumber).getUserById(deny), deny)).append("\n");
            }
            msg.appendCodeLine(sb.toString());
            e.reply(msg.build()).setEphemeral(true).queue();
        } else if ("deny".equals(e.getName()) && "add".equals(e.getSubcommandName())) {
            if (!checkNeedAdmin(e.getMember(), e))
                return;
            if (!DiscordUtils.hasPermission(e.getMember())) {
                e.reply("ユーザを読み上げ拒否する権限がありません").setEphemeral(true).queue();
                return;
            }
            var uop = e.getOption("user");
            if (uop == null) {
                e.reply("ユーザを指定してください").setEphemeral(true).queue();
                return;
            }
            if (uop.getAsUser().isBot()) {
                e.reply(DiscordUtils.getName(botNumber, e.getGuild(), uop.getAsUser(), uop.getAsUser().getIdLong()) + "はBOTです").setEphemeral(true).queue();
                return;
            }

            if (Main.SAVE_DATA.isDenyUser(e.getGuild().getIdLong(), uop.getAsUser().getIdLong())) {
                e.reply("すでに読み上げ拒否をされているユーザです").setEphemeral(true).queue();
                return;
            }
            Main.SAVE_DATA.addDenyUser(e.getGuild().getIdLong(), uop.getAsUser().getIdLong());
            e.reply(DiscordUtils.getName(botNumber, e.getGuild(), uop.getAsUser(), uop.getAsUser().getIdLong()) + "の読み上げ拒否します").setEphemeral(true).queue();
        } else if ("deny".equals(e.getName()) && "remove".equals(e.getSubcommandName())) {
            if (!checkNeedAdmin(e.getMember(), e))
                return;
            if (!DiscordUtils.hasPermission(e.getMember())) {
                e.reply("ユーザの読み上げ拒否を解除する権限がありません").setEphemeral(true).queue();
                return;
            }
            var uop = e.getOption("user");
            if (uop == null) {
                e.reply("ユーザを指定してください").setEphemeral(true).queue();
                return;
            }
            if (uop.getAsUser().isBot()) {
                e.reply(DiscordUtils.getName(botNumber, e.getGuild(), uop.getAsUser(), uop.getAsUser().getIdLong()) + "はBOTです").setEphemeral(true).queue();
                return;
            }
            if (!Main.SAVE_DATA.isDenyUser(e.getGuild().getIdLong(), uop.getAsUser().getIdLong())) {
                e.reply("読み上げ拒否をされていないユーザです").setEphemeral(true).queue();
                return;
            }
            Main.SAVE_DATA.removeDenyUser(e.getGuild().getIdLong(), uop.getAsUser().getIdLong());
            e.reply(DiscordUtils.getName(botNumber, e.getGuild(), uop.getAsUser(), uop.getAsUser().getIdLong()) + "の読み上げ拒否を解除します").setEphemeral(true).queue();
        } else if ("inm".equals(e.getName())) {
            var op = e.getOption("search");
            if (op != null && TTSManager.getInstance().getVoiceTypes(e.getUser().getIdLong(), e.getGuild().getIdLong()).contains(INMManager.getInstance().getVoice())) {
                TTSManager.getInstance().sayText(new BotAndGuild(botNumber, e.getGuild().getIdLong()), INMManager.getInstance().getVoice(), op.getAsString());
            }
            e.deferReply().queue();
            e.getHook().deleteOriginal().queue();
        } else if ("config".equals(e.getName())) {
            if (!checkNeedAdmin(e.getMember(), e))
                return;
            var sb = e.getSubcommandName();
            if (sb == null || sb.isEmpty()) {
                e.reply("コンフィグが未指定です").setEphemeral(true).queue();
                return;
            }
            var sc = Main.getServerConfig(e.getGuild().getIdLong());
            if ("show".equals(sb)) {
                var msg = new MessageBuilder().append("現在のコンフィグ\n");
                StringBuilder sbr = new StringBuilder();

                sbr.append("VCに参加時のみ読み上げ").append(" ").append(sc.isNeedJoin() ? "有効" : "無効").append("\n");
                sbr.append("読み上げの上書き").append(" ").append(sc.isOverwriteAloud() ? "有効" : "無効").append("\n");
                if (!DiscordUtils.isNonAllowInm(e.getGuild().getIdLong()))
                    sbr.append("INMモード").append(" ").append(sc.isInmMode(e.getGuild().getIdLong()) ? "有効" : "無効").append("\n");

                msg.appendCodeLine(sbr.toString());
                e.reply(msg.build()).setEphemeral(true).queue();
            } else {
                var en = e.getOption("enable");
                if (en == null)
                    en = e.getOption("max-count");
                if (en == null) {
                    e.reply("コンフィグ設定内容が未指定です").setEphemeral(true).queue();
                    return;
                }
                if (en.getType() == OptionType.BOOLEAN) {
                    boolean ena = en.getAsBoolean();
                    String enStr = ena ? "有効" : "無効";
                    switch (sb) {
                        case "need-join" -> {
                            if (sc.isNeedJoin() == ena) {
                                e.reply("すでにVCに参加時のみ読み上げは" + enStr + "です").setEphemeral(true).queue();
                                return;
                            }
                            sc.setNeedJoin(ena);
                            e.reply("VCに参加時のみ読み上げを" + enStr + "にしました").queue();
                        }
                        case "overwrite-aloud" -> {
                            if (sc.isOverwriteAloud() == ena) {
                                e.reply("すでに読み上げの上書きは" + enStr + "です").setEphemeral(true).queue();
                                return;
                            }
                            sc.setOverwriteAloud(ena);
                            e.reply("読み上げの上書きを" + enStr + "にしました").queue();
                        }
                        case "inm-mode" -> {
                            if (ena && DiscordUtils.isNonAllowInm(e.getGuild().getIdLong())) {
                                if (rand.nextInt() == 0) {
                                    e.reply("ｲﾔｰキツイッス（素）").queue();
                                } else {
                                    e.reply("ダメみたいですね").queue();
                                }
                                return;
                            }
                            if (sc.isInmMode(e.getGuild().getIdLong()) == ena) {
                                e.reply("すでにINMモードは" + enStr + "です").setEphemeral(true).queue();
                                return;
                            }
                            sc.setInmMode(ena);
                            e.reply("INMモードを" + enStr + "にしました").queue();
                        }
                        case "join-say-name" -> {
                            if (sc.isJoinSayName() == ena) {
                                e.reply("すでにVCに参加時に名前を読み上げは" + enStr + "です").setEphemeral(true).queue();
                                return;
                            }
                            sc.setJoinSayName(ena);
                            e.reply("VCに参加時に名前を読み上げを" + enStr + "にしました").queue();
                        }
                    }
                } else if (en.getType() == OptionType.INTEGER) {
                    int iv = en.getAsInt();
                    switch (sb) {
                        case "read-around-limit" -> {
                            if (sc.getMaxReadAroundCharacterLimit() == iv) {
                                e.reply("すでに最大読み上げ文字数は" + iv + "です").setEphemeral(true).queue();
                                return;
                            }
                            sc.setMaxReadAroundCharacterLimit(iv);
                            e.reply("最大読み上げ文字数を" + iv + "にしました").queue();
                        }
                    }
                }
            }
        } else if ("vnick".equals(e.getName())) {
            var uop = e.getOption("user");
            if (uop != null && DiscordUtils.hasPermission(e.getMember())) {
                e.reply("他ユーザーの読み上げユーザ名を変更するための権限がありません").queue();
                return;
            }
            User user = uop == null ? e.getUser() : uop.getAsUser();
            if (user.isBot()) {
                e.reply(DiscordUtils.getName(botNumber, e.getGuild(), user, user.getIdLong()) + "はBOTです").queue();
                return;
            }

            var nm = e.getOption("name");
            if (nm != null) {
                var name = nm.getAsString();
                if ("reset".equals(name)) {
                    Main.SAVE_DATA.removeUserNickName(user.getIdLong());
                    e.reply(DiscordUtils.getName(botNumber, e.getGuild(), user, user.getIdLong()) + "のニックネームをリセットしました").queue();
                } else {
                    Main.SAVE_DATA.setUserNickName(user.getIdLong(), name);
                    e.reply(DiscordUtils.getName(botNumber, e.getGuild(), user, user.getIdLong()) + "のニックネームを変更しました").queue();
                }
            }

        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent e) {
        if ("voice".equals(e.getName()) && "change".equals(e.getSubcommandName())) {
            var op = e.getInteraction().getOption("voice_type");
            String str = op == null ? null : op.getAsString();

            List<VoiceType> choices = new ArrayList<>();

            for (VoiceType voiceType : TTSManager.getInstance().getVoiceTypes(e.getUser().getIdLong(), e.getGuild().getIdLong())) {
                if (str != null && (voiceType.getId().contains(str) || voiceType.getTitle().contains(str)))
                    choices.add(voiceType);
            }

            if (choices.isEmpty()) {
                choices.addAll(TTSManager.getInstance().getVoiceTypes(e.getUser().getIdLong(), e.getGuild().getIdLong()));
            }

            choices = choices.stream().sorted(Comparator.comparingInt(n -> {
                if (str == null) return 0;
                int i = TextUtils.getComplementPoint(n.getId(), str) * 2;
                int t = TextUtils.getComplementPoint(n.getTitle(), str);
                return i + t;
            })).toList();

            if (choices.size() > 25) {
                List<VoiceType> nc = new ArrayList<>();
                int ct = 0;
                for (VoiceType choice : choices) {
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
        if (tm.getTTSChanel(new BotAndGuild(botNumber, e.getGuild().getIdLong())) == e.getChannel().getIdLong() && !e.getMember().getUser().isBot()) {
            if (Main.SAVE_DATA.isDenyUser(e.getGuild().getIdLong(), e.getMember().getIdLong())) return;
            if (Main.getServerConfig(e.getGuild().getIdLong()).isNeedJoin()) {
                var vs = e.getMember().getVoiceState();
                if (vs == null) return;
                var vc = vs.getChannel();
                if (vc == null) return;
                if (vc.getIdLong() != TTSManager.getInstance().getTTSVoiceChanel(e.getGuild()))
                    return;
            }
            tm.sayChat(new BotAndGuild(botNumber, e.getGuild().getIdLong()), e.getMember().getUser().getIdLong(), e.getMessage().getContentRaw());
            for (Message.Attachment attachment : e.getMessage().getAttachments()) {
                if (!attachment.isImage() && !attachment.isVideo())
                    tm.sayText(new BotAndGuild(botNumber, e.getGuild().getIdLong()), tm.getUserVoiceType(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong()), attachment.getFileName());
            }
        }
    }

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
        if (!Main.getServerConfig(event.getGuild().getIdLong()).isJoinSayName()) return;

        var tm = TTSManager.getInstance();
        long vc = tm.getTTSVoiceChanel(event.getGuild());
        if (vc != event.getChannelJoined().getIdLong()) return;

        tm.sayText(new BotAndGuild(botNumber, event.getGuild().getIdLong()), event.getMember().getIdLong(), new VCEventSayVoice(VCEventSayVoice.EventType.JOIN, FNPair.of(event.getGuild(), botNumber), event.getMember().getUser()));
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        if (!Main.getServerConfig(event.getGuild().getIdLong()).isJoinSayName()) return;

        var tm = TTSManager.getInstance();
        long vc = tm.getTTSVoiceChanel(event.getGuild());
        if (vc != event.getChannelLeft().getIdLong()) return;

        tm.sayText(new BotAndGuild(botNumber, event.getGuild().getIdLong()), event.getMember().getIdLong(), new VCEventSayVoice(VCEventSayVoice.EventType.LEAVE, FNPair.of(event.getGuild(), botNumber), event.getMember().getUser()));
    }

    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {
        if (event.getMember().getUser().isBot() && Main.getJDAByID(event.getMember().getIdLong()) != null)
            TTSManager.getInstance().reconnect(BotAndGuild.ofId(event.getMember().getIdLong(), event.getGuild().getIdLong()), event.getChannelJoined().getIdLong());

        if (!Main.getServerConfig(event.getGuild().getIdLong()).isJoinSayName()) return;

        var tm = TTSManager.getInstance();
        long vc = tm.getTTSVoiceChanel(event.getGuild());
        if (vc == event.getChannelLeft().getIdLong()) {
            tm.sayText(new BotAndGuild(botNumber, event.getGuild().getIdLong()), event.getMember().getIdLong(), new VCEventSayVoice(VCEventSayVoice.EventType.MOVE_TO, FNPair.of(event.getGuild(), botNumber), event.getMember().getUser(), event));
        } else if (vc == event.getChannelJoined().getIdLong()) {
            tm.sayText(new BotAndGuild(botNumber, event.getGuild().getIdLong()), event.getMember().getIdLong(), new VCEventSayVoice(VCEventSayVoice.EventType.MOVE_FROM, FNPair.of(event.getGuild(), botNumber), event.getMember().getUser(), event));
        }
    }

    private boolean checkNeedAdmin(Member member, IReplyCallback callback) {
        if (!DiscordUtils.hasNeedAdminPermission(member)) {
            callback.reply("コマンドを実行する権限がありません").setEphemeral(true).queue();
            return false;
        }
        return true;
    }

    private class ReconnectThread extends Thread {
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
            TTSManager.getInstance().connect(new BotAndGuild(botNumber, guild.getIdLong()), textChannel.getIdLong(), channel.getIdLong());
        }
    }
}
