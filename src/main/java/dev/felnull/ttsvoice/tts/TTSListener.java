package dev.felnull.ttsvoice.tts;

import dev.felnull.ttsvoice.Main;
import dev.felnull.ttsvoice.discord.BotLocation;
import dev.felnull.ttsvoice.tts.sayedtext.StartupSayedText;
import dev.felnull.ttsvoice.tts.sayedtext.VCEventSayedText;
import dev.felnull.ttsvoice.tts.tracker.TextMessageTTSTracker;
import dev.felnull.ttsvoice.util.DiscordUtils;
import dev.felnull.ttsvoice.voice.reinoare.cookie.CookieManager;
import dev.felnull.ttsvoice.voice.reinoare.inm.INMManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;

public class TTSListener extends ListenerAdapter {
    private static final Logger LOGGER = LogManager.getLogger(TTSListener.class);
    public static HashMap<Long, HashMap<ActionType, List<AuditLogEntry>>> auditLogs = new HashMap<>();

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent e) {
        if (!e.isFromGuild() || e.getMember() == null) return;

        switch (e.getName()) {
            case "join" -> TTSCommands.join(e);
            case "leave" -> TTSCommands.leave(e);
            case "reconnect" -> TTSCommands.reconnect(e);
            case "inm" -> TTSCommands.playReinoare(e, INMManager.getInstance());
            case "cookie" -> TTSCommands.playReinoare(e, CookieManager.getInstance());
            case "vnick" -> TTSCommands.vnick(e);
            case "about" -> TTSCommands.about(e);
        }

        if (e.getSubcommandName() != null) {
            switch (e.getName()) {
                case "voice" -> {
                    switch (e.getSubcommandName()) {
                        case "show" -> TTSCommands.voiceShow(e);
                        case "change" -> TTSCommands.voiceChange(e);
                        case "check" -> TTSCommands.voiceCheck(e);
                    }
                }
                case "deny" -> {
                    switch (e.getSubcommandName()) {
                        case "show" -> TTSCommands.denyShow(e);
                        case "add" -> TTSCommands.denyAdd(e);
                        case "remove" -> TTSCommands.denyRemove(e);
                    }
                }
                case "config" -> {
                    var sb = e.getSubcommandName();
                    if ("show".equals(sb)) {
                        TTSCommands.configShow(e);
                    } else {
                        TTSCommands.configSet(e, sb);
                    }
                }
                case "dict" -> {
                    switch (e.getSubcommandName()) {
                        case "show" -> TTSCommands.dictShow(e);
                        case "add" -> TTSCommands.dictAdd(e);
                        case "remove" -> TTSCommands.dictRemove(e);
                        case "download" -> TTSCommands.dictDownload(e);
                        case "upload" -> TTSCommands.dictUpload(e);
                    }
                }
            }
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent e) {
        if (!e.isFromGuild() || e.getMember() == null) return;

        if ("voice".equals(e.getName()) && "change".equals(e.getSubcommandName())) {
            TTSCommandAutoCompletes.voiceChange(e);
        } else if ("inm".equals(e.getName())) {
            TTSCommandAutoCompletes.playReinoare(e, INMManager.getInstance());
        } else if ("cookie".equals(e.getName())) {
            TTSCommandAutoCompletes.playReinoare(e, CookieManager.getInstance());
        } else if ("dict".equals(e.getName()) && "remove".equals(e.getSubcommandName())) {
            TTSCommandAutoCompletes.dictRemove(e);
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        if (!e.isFromGuild() || e.getMember() == null) return;

        var sc = Main.getServerSaveData(e.getGuild().getIdLong());
        var tm = TTSManager.getInstance();
        if (tm.getTTSChanel(BotLocation.of(e)) == e.getChannel().getIdLong() && !e.getMember().getUser().isBot()) {
            if (Main.getSaveData().isDenyUser(e.getGuild().getIdLong(), e.getMember().getIdLong())) return;
            if (e.getMessage().getContentRaw().startsWith(sc.getNonReadingPrefix())) return;
            if (Main.getServerSaveData(e.getGuild().getIdLong()).isNeedJoin()) {
                var vs = e.getMember().getVoiceState();
                if (vs == null) return;
                var vc = vs.getChannel();
                if (vc == null) return;
                if (vc.getIdLong() != TTSManager.getInstance().getTTSVoiceChanel(e.getGuild())) return;
            }
            tm.sayChat(BotLocation.of(e), e.getMember().getUser().getIdLong(), e.getMessage().getContentRaw(), e.getChannel().getIdLong(), e.getMessage().getIdLong());
            for (Message.Attachment attachment : e.getMessage().getAttachments()) {
                if (!attachment.isImage() && !attachment.isVideo())
                    tm.sayText(BotLocation.of(e), tm.getUserVoiceType(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong()), attachment.getFileName());
            }
        }
    }

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
        if (!Main.getServerSaveData(event.getGuild().getIdLong()).isJoinSayName()) return;
        var vc = event.getGuild().getAudioManager().getConnectedChannel();

        if (vc == event.getChannelJoined()) {
            if (event.getMember().getUser().isBot())
                sayText(event, VCEventSayedText.EventType.CONNECT);
            else
                sayText(event, VCEventSayedText.EventType.JOIN);
        }
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        if (!Main.getServerSaveData(event.getGuild().getIdLong()).isJoinSayName()) return;
        var vc = event.getGuild().getAudioManager().getConnectedChannel();

        if (vc == event.getChannelLeft()) {
            if (canViewAuditLog(event.getGuild())) {
                boolean wasKicked = wasAuditLogChanged(event.getGuild(), ActionType.MEMBER_VOICE_KICK);
                if (wasKicked) sayText(event, VCEventSayedText.EventType.FORCE_LEAVE);
                else sayText(event, VCEventSayedText.EventType.LEAVE);
            } else {
                sayText(event, VCEventSayedText.EventType.LEAVE);
            }
        }
        updateAuditLogMap(event.getGuild());
    }

    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {
        if (event.getMember().getUser().equals(event.getJDA().getSelfUser()))
            TTSManager.getInstance().reconnect(new BotLocation(event.getMember().getIdLong(), event.getGuild().getIdLong()), event.getChannelJoined().getIdLong());

        if (!Main.getServerSaveData(event.getGuild().getIdLong()).isJoinSayName()) return;
        var vc = event.getGuild().getAudioManager().getConnectedChannel();
        if (vc == event.getChannelJoined() || vc == event.getChannelLeft()) {
            if (canViewAuditLog(event.getGuild())) {
                boolean wasMoved = wasAuditLogChanged(event.getGuild(), ActionType.MEMBER_VOICE_MOVE);
                //  System.out.println(wasMoved);
                if (vc == event.getChannelLeft()) {
                    if (wasMoved) sayText(event, VCEventSayedText.EventType.FORCE_MOVE_TO);
                    else sayText(event, VCEventSayedText.EventType.MOVE_TO);
                } else if (vc == event.getChannelJoined()) {
                    if (wasMoved) sayText(event, VCEventSayedText.EventType.FORCE_MOVE_FROM);
                    else sayText(event, VCEventSayedText.EventType.MOVE_FROM);
                }
            } else {
                if (vc == event.getChannelLeft()) {
                    sayText(event, VCEventSayedText.EventType.MOVE_TO);
                } else if (vc == event.getChannelJoined()) {
                    sayText(event, VCEventSayedText.EventType.MOVE_FROM);
                }
            }
        }
        if (getLogSeeableActiveJDAs(event.getGuild()).size() >= 1 && getLogSeeableActiveJDAs(event.getGuild()).get(0).equals(event.getJDA()))
            updateAuditLogMap(event.getGuild());
    }

    public static List<JDA> getLogSeeableActiveJDAs(Guild guild) {
        return Main.getActiveJDAs(guild).stream().filter(jda -> canViewAuditLog(guild, jda)).toList();
    }

    public static boolean canViewAuditLog(Guild guild) {
        return canViewAuditLog(guild, guild.getJDA());
    }

    public static boolean canViewAuditLog(Guild guild, JDA jda) {
        return guild.getMember(jda.getSelfUser()).hasPermission(Permission.VIEW_AUDIT_LOGS);
    }

    public static void updateAuditLogMap(Guild guild) {
        if (!canViewAuditLog(guild)) return;
        var map = auditLogs.get(guild.getIdLong()) != null ? auditLogs.get(guild.getIdLong()) : new HashMap<ActionType, List<AuditLogEntry>>();
        map.put(ActionType.MEMBER_VOICE_MOVE, guild.retrieveAuditLogs().type(ActionType.MEMBER_VOICE_MOVE).limit(10).complete());
        map.put(ActionType.MEMBER_VOICE_KICK, guild.retrieveAuditLogs().type(ActionType.MEMBER_VOICE_KICK).limit(10).complete());
        auditLogs.put(guild.getIdLong(), map);
    }

    public static boolean wasAuditLogChanged(Guild guild, ActionType type) {
        var logNew = guild.retrieveAuditLogs().type(type).limit(10).complete();
        var logOld = auditLogs.get(guild.getIdLong()).get(type);
        return !isEqualAuditLog(logNew, logOld);
    }

    public static boolean isEqualAuditLog(List<AuditLogEntry> log1, List<AuditLogEntry> log2) {
        var largerLog = new ArrayList<>(log1.size() > log2.size() ? log1 : log2);
        var smallerLog = new ArrayList<>(log1.size() <= log2.size() ? log1 : log2);
        IntStream.range(smallerLog.size(), largerLog.size()).forEach(i -> largerLog.remove(smallerLog.size()));

        if (!largerLog.equals(smallerLog)) return false;
        for (int i = 0; i < largerLog.size(); i++) {
            for (String key : largerLog.get(i).getOptions().keySet()) {
                if (!largerLog.get(i).getOptionByName(key).equals(smallerLog.get(i).getOptionByName(key))) return false;
            }
        }
        return true;
    }

    public void sayText(GuildVoiceUpdateEvent event, VCEventSayedText.EventType type) {
        TTSManager.getInstance().sayText(BotLocation.of(event), event.getMember().getIdLong(), new VCEventSayedText(type, BotLocation.of(event), event.getMember().getUser(), event));
    }

    private boolean checkNeedAdmin(Member member, IReplyCallback callback) {
        if (!DiscordUtils.hasNeedAdminPermission(member)) {
            callback.reply("コマンドを実行する権限がありません").setEphemeral(true).queue();
            return false;
        }
        return true;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
        var jda = event.getJDA();
        String name = jda.getSelfUser().getName();

        for (Guild guild : jda.getGuilds()) {
            Main.updateGuildCommand(guild, false);
        }

        synchronized (Main.getAllServerSaveData()) {
            Main.getAllServerSaveData().forEach((g, c) -> {
                long id = jda.getSelfUser().getIdLong();
                var lj = c.getLastJoinChannel(id);
                if (lj != null) {
                    try {
                        var guild = jda.getGuildById(g);
                        if (guild == null)
                            return;
                        var audioManager = guild.getAudioManager();
                        var achn = guild.getChannelById(AudioChannel.class, lj.audioChannel());
                        if (achn == null) return;
                        var tch = guild.getChannelById(TextChannel.class, lj.ttsChannel());
                        if (tch == null) return;

                        audioManager.openAudioConnection(achn);
                        var bag = BotLocation.of(event, guild);
                        var tm = TTSManager.getInstance();
                        if (Main.getServerSaveData(bag.getGuild().getIdLong()).isJoinSayName())
                            TTSListener.updateAuditLogMap(bag.getGuild());
                        tm.connect(bag, tch.getIdLong(), achn.getIdLong());

                        var v = Main.VERSION;
                        var lv = Main.getSaveData().getLastVersion();

                        if (v.equals(lv)) {
                            v = null;
                            lv = null;
                        } else if (lv != null && lv.isEmpty()) {
                            lv = null;
                        }

                        Main.getSaveData().setLastVersion(Main.VERSION);

                        tm.saySystemText(bag, new StartupSayedText(name, lv, v, System.currentTimeMillis() - Main.getSaveData().getLastTime() <= 60 * 1000));

                        LOGGER.info(name + " reconnect to " + guild.getName());
                    } catch (Exception ex) {
                        LOGGER.error("Oh... reconnection failed", ex);
                    }
                }
            });
        }
    }

    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        if (!event.isFromGuild()) return;

        var botLocation = BotLocation.of(event);
        var mtkey = new TTSManager.TextMessageTTSTrackerKey(botLocation, event.getChannel().getIdLong(), event.getMessageIdLong());
        final var trackers = TTSManager.getInstance().getTrackers();
        TextMessageTTSTracker e;
        synchronized (trackers) {
            e = trackers.get(mtkey);
        }
        if (e != null)
            e.onUpdateMessage(null, -1);
    }

    @Override
    public void onMessageUpdate(@NotNull MessageUpdateEvent event) {
        if (!event.isFromGuild() || event.getMember() == null) return;

        var botLocation = BotLocation.of(event);
        var mtkey = new TTSManager.TextMessageTTSTrackerKey(botLocation, event.getChannel().getIdLong(), event.getMessageIdLong());
        final var trackers = TTSManager.getInstance().getTrackers();
        TextMessageTTSTracker e;
        synchronized (trackers) {
            e = trackers.get(mtkey);
        }
        if (e != null)
            e.onUpdateMessage(event.getMessage().getContentRaw(), event.getMessage().getAuthor().getIdLong());
    }

}
