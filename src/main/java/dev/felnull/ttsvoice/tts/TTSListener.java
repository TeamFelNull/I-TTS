package dev.felnull.ttsvoice.tts;

import dev.felnull.fnjl.tuple.FNPair;
import dev.felnull.ttsvoice.Main;
import dev.felnull.ttsvoice.tts.sayvoice.VCEventSayVoice;
import dev.felnull.ttsvoice.util.DiscordUtils;
import dev.felnull.ttsvoice.voice.HasTitleAndID;
import dev.felnull.ttsvoice.voice.VoiceCategory;
import dev.felnull.ttsvoice.voice.VoiceType;
import dev.felnull.ttsvoice.voice.reinoare.cookie.CookieEntry;
import dev.felnull.ttsvoice.voice.reinoare.cookie.CookieManager;
import dev.felnull.ttsvoice.voice.reinoare.inm.INMEntry;
import dev.felnull.ttsvoice.voice.reinoare.inm.INMManager;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
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

import java.util.*;
import java.util.stream.IntStream;

public class TTSListener extends ListenerAdapter {
    private static final Random rand = new Random();
    private final int botNumber;
    public static HashMap<Long, HashMap<ActionType, List<AuditLogEntry>>> auditLogs = new HashMap<>();

    public TTSListener(int botNumber) {
        this.botNumber = botNumber;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent e) {
        switch (e.getName()) {
            case "join" -> {
                if (!checkNeedAdmin(e.getMember(), e))
                    return;
                var channel = e.getInteraction().getOption("channel");
                AudioChannel audioChannel;
                if (channel != null) {
                    audioChannel = channel.getAsChannel().asAudioChannel();
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
            }
            case "leave" -> {
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
            }
            case "reconnect" -> {
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
            }
            case "voice" -> {
                switch (e.getSubcommandName()) {
                    case "list" -> {
                        var msg = new MessageBuilder().append("読み上げ音声タイプ一覧\n");
                        StringBuilder sb = new StringBuilder();
                        for (VoiceType voiceType : TTSManager.getInstance().getVoiceTypes(e.getUser().getIdLong(), e.getGuild().getIdLong())) {
                            sb.append(voiceType.getId()).append(" ").append(voiceType.getTitle()).append("\n");
                        }
                        msg.appendCodeLine(sb.toString());
                        e.reply(msg.build()).setEphemeral(true).queue();
                    }
                    case "change" -> {
                        var uop = e.getOption("user");
                        if (uop != null && !DiscordUtils.hasPermission(e.getMember())) {
                            e.reply("他ユーザーを編集するための権限がありません").queue();
                            return;
                        }
                        User user = uop == null ? e.getUser() : uop.getAsUser();

/*                        if (user.isBot()) {
                            e.reply(DiscordUtils.getName(botNumber, e.getGuild(), user, user.getIdLong()) + "はBOTです").queue();
                            return;
                        }*/

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
                    }
                    case "check" -> {
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
                    }
                }
            }
            case "deny" -> {
                switch (e.getSubcommandName()) {
                    case "list" -> {
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
                    }
                    case "add" -> {
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
                    }
                    case "remove" -> {
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
                    }
                }
            }
            case "inm" -> {
                var op = e.getOption("search");
                if (op != null && TTSManager.getInstance().getVoiceTypes(e.getUser().getIdLong(), e.getGuild().getIdLong()).contains(INMManager.getInstance().getVoice())) {
                    TTSManager.getInstance().sayText(new BotAndGuild(botNumber, e.getGuild().getIdLong()), INMManager.getInstance().getVoice(), op.getAsString());
                }
                e.deferReply().queue();
                e.getHook().deleteOriginal().queue();
            }
            case "cookie" -> {
                var op = e.getOption("search");
                if (op != null && TTSManager.getInstance().getVoiceTypes(e.getUser().getIdLong(), e.getGuild().getIdLong()).contains(CookieManager.getInstance().getVoice())) {
                    TTSManager.getInstance().sayText(new BotAndGuild(botNumber, e.getGuild().getIdLong()), CookieManager.getInstance().getVoice(), op.getAsString());
                }
                e.deferReply().queue();
                e.getHook().deleteOriginal().queue();
            }
            case "config" -> {
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
                    if (!DiscordUtils.isNonAllowCookie(e.getGuild().getIdLong()))
                        sbr.append("クッキー☆モード").append(" ").append(sc.isCookieMode(e.getGuild().getIdLong()) ? "有効" : "無効").append("\n");

                    msg.appendCodeLine(sbr.toString());
                    e.reply(msg.build()).setEphemeral(true).queue();
                } else {
                    var en = e.getOption("enable");
                    if (en == null)
                        en = e.getOption("max-count");
                    if (en == null)
                        en = e.getOption("prefix");
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
                            case "cookie-mode" -> {
                                if (ena && DiscordUtils.isNonAllowCookie(e.getGuild().getIdLong())) {
                                    if (rand.nextInt() == 0) {
                                        e.reply("くぉら！").queue();
                                    } else {
                                        e.reply("お覚悟を。").queue();
                                    }
                                    return;
                                }
                                if (sc.isCookieMode(e.getGuild().getIdLong()) == ena) {
                                    e.reply("すでにクッキー☆モードは" + enStr + "です").setEphemeral(true).queue();
                                    return;
                                }
                                sc.setCookieMode(ena);
                                e.reply("クッキー☆モードを" + enStr + "にしました").queue();
                            }
                            case "join-say-name" -> {
                                if (sc.isJoinSayName() == ena) {
                                    e.reply("すでにVCに参加時に名前を読み上げは" + enStr + "です").setEphemeral(true).queue();
                                    return;
                                }
                                sc.setJoinSayName(ena);
                                if (ena) {
                                    updateAuditLogMap(e.getGuild());
                                }
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
                    } else if (en.getType() == OptionType.STRING) {
                        String pre = en.getAsString();
                        switch (sb) {
                            case "non-reading-prefix" -> {
                                if (sc.getNonReadingPrefix().equals(pre)) {
                                    e.reply("既に先頭につけると読み上げなくなる文字は" + DiscordUtils.toNoMention(pre) + "です").setEphemeral(true).queue();
                                    return;
                                }
                                sc.setNonReadingPrefix(pre);
                                e.reply("先頭につけると読み上げなくなる文字を" + DiscordUtils.toNoMention(pre) + "に設定しました").queue();
                            }
                        }
                    }
                }
            }
            case "vnick" -> {
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
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent e) {
        if ("voice".equals(e.getName()) && "change".equals(e.getSubcommandName())) {
            var opc = e.getInteraction().getOption("voice_category");
            var opt = e.getInteraction().getOption("voice_type");
            String strc = opc == null ? null : opc.getAsString();
            String strt = opt == null ? null : opt.getAsString();
            var choices = new ArrayList<HasTitleAndID>();
            if (strt == null) {
                for (VoiceCategory voiceCategory : TTSManager.getInstance().getVoiceCategories(e.getUser().getIdLong(), e.getGuild().getIdLong())) {
                    if (voiceCategory.getId().contains(strc) || voiceCategory.getTitle().contains(strc))
                        choices.add(voiceCategory);
                }
            } else {
                for (VoiceType voiceType : TTSManager.getInstance().getVoiceTypes(e.getUser().getIdLong(), e.getGuild().getIdLong())) {
                    if (voiceType.getId().contains(strc) && (voiceType.getId().contains(strt) || voiceType.getTitle().contains(strt)))
                        choices.add(voiceType);
                }
            }

            e.replyChoices(choices.stream().limit(25).map(n -> new Command.Choice(n.getTitle(), n.getId())).toList()).queue();
        } else if ("inm".equals(e.getName())) {
            var op = e.getInteraction().getOption("search");
            var entries = new ArrayList<INMEntry>();
            if (op != null && TTSManager.getInstance().getVoiceTypes(e.getUser().getIdLong(), e.getGuild().getIdLong()).contains(INMManager.getInstance().getVoice())) {
                var im = INMManager.getInstance();
                try {
                    var scr = im.search(op.getAsString(), 25);
                    scr = im.sort(scr);
                    entries.addAll(scr);
                } catch (Exception ignored) {
                }
            }

            e.replyChoices(entries.stream().map(n -> new Command.Choice(n.name(), n.name())).toList()).queue();
        } else if ("cookie".equals(e.getName())) {
            var op = e.getInteraction().getOption("search");
            var entries = new ArrayList<CookieEntry>();
            if (op != null && TTSManager.getInstance().getVoiceTypes(e.getUser().getIdLong(), e.getGuild().getIdLong()).contains(CookieManager.getInstance().getVoice())) {
                var im = CookieManager.getInstance();
                try {
                    var scr = im.search(op.getAsString(), 25);
                    scr = im.sort(scr);
                    entries.addAll(scr);
                } catch (Exception ignored) {
                }
            }

            e.replyChoices(entries.stream().map(n -> new Command.Choice(n.name(), n.name())).toList()).queue();
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        var sc = Main.getServerConfig(e.getGuild().getIdLong());
        var tm = TTSManager.getInstance();
        if (tm.getTTSChanel(new BotAndGuild(botNumber, e.getGuild().getIdLong())) == e.getChannel().getIdLong() && !e.getMember().getUser().isBot()) {
            if (Main.SAVE_DATA.isDenyUser(e.getGuild().getIdLong(), e.getMember().getIdLong())) return;
            if (e.getMessage().getContentRaw().startsWith(sc.getNonReadingPrefix())) return;
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
        var vc = event.getGuild().getAudioManager().getConnectedChannel();
        if (vc == null) return;

        if (vc == event.getChannelJoined())
            tmSayText(event, VCEventSayVoice.EventType.JOIN);
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        if (!Main.getServerConfig(event.getGuild().getIdLong()).isJoinSayName()) return;
        var vc = event.getGuild().getAudioManager().getConnectedChannel();
        if (vc == null) return;

        if (vc == event.getChannelLeft()) {
            boolean wasKicked = wasAuditLogChanged(event.getGuild(), ActionType.MEMBER_VOICE_KICK);
            if (wasKicked) tmSayText(event, VCEventSayVoice.EventType.FORCE_LEAVE);
            else tmSayText(event, VCEventSayVoice.EventType.LEAVE);
        }
        updateAuditLogMap(event.getGuild());
    }

    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {
        if (!Main.getServerConfig(event.getGuild().getIdLong()).isJoinSayName()) return;
        var vc = event.getGuild().getAudioManager().getConnectedChannel();
        if (vc == null) return;

        if (event.getMember().getUser().isBot() && Main.getJDAByID(event.getMember().getIdLong()) != null)
            TTSManager.getInstance().reconnect(BotAndGuild.ofId(event.getMember().getIdLong(), event.getGuild().getIdLong()), event.getChannelJoined().getIdLong());

        if (vc == event.getChannelJoined() || vc == event.getChannelLeft()) {
            boolean wasMoved = wasAuditLogChanged(event.getGuild(), ActionType.MEMBER_VOICE_MOVE);
            if (vc == event.getChannelLeft()) {
                if (wasMoved) tmSayText(event, VCEventSayVoice.EventType.FORCE_MOVE_TO);
                else tmSayText(event, VCEventSayVoice.EventType.MOVE_TO);
            } else if (vc == event.getChannelJoined()) {
                if (wasMoved) tmSayText(event, VCEventSayVoice.EventType.FORCE_MOVE_FROM);
                else tmSayText(event, VCEventSayVoice.EventType.MOVE_FROM);
            }
        }
        updateAuditLogMap(event.getGuild());
    }

    public static void updateAuditLogMap(Guild guild) {
        var map = auditLogs.get(guild.getIdLong()) != null ? auditLogs.get(guild.getIdLong()) : new HashMap<ActionType, List<AuditLogEntry>>();
        map.put(ActionType.MEMBER_VOICE_MOVE, guild.retrieveAuditLogs().type(ActionType.MEMBER_VOICE_MOVE).limit(10).complete());
        map.put(ActionType.MEMBER_VOICE_KICK, guild.retrieveAuditLogs().type(ActionType.MEMBER_VOICE_KICK).limit(10).complete());
        auditLogs.put(guild.getIdLong(), map);
    }

    public static boolean wasAuditLogChanged(Guild guild, ActionType type) {
        var logNew = guild.retrieveAuditLogs().type(type).limit(10).complete();
        ;
        var logOld = auditLogs.get(guild.getIdLong()).get(type);
        return !isEqualAuditLog(logNew, logOld);
    }

    public static boolean isEqualAuditLog(List<AuditLogEntry> log1, List<AuditLogEntry> log2) {
        var largerLog = new ArrayList<>(log1.size() > log2.size() ? log1 : log2);
        var smallerLog = new ArrayList<>(log1.size() <= log2.size() ? log1 : log2);
        IntStream.range(smallerLog.size(), largerLog.size()).forEach(i -> largerLog.remove(smallerLog.size()));

        if (!largerLog.equals(smallerLog))
            return false;
        for (int i = 0; i < largerLog.size(); i++) {
            for (String key : largerLog.get(i).getOptions().keySet()) {
                if (!largerLog.get(i).getOptionByName(key).equals(smallerLog.get(i).getOptionByName(key)))
                    return false;
            }
        }
        return true;
    }

    public void tmSayText(GuildVoiceUpdateEvent event, VCEventSayVoice.EventType type) {
        TTSManager.getInstance().sayText(new BotAndGuild(botNumber, event.getGuild().getIdLong()), event.getMember().getIdLong(), new VCEventSayVoice(type, FNPair.of(event.getGuild(), botNumber), event.getMember().getUser(), event));
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
