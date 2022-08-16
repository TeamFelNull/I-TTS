package dev.felnull.ttsvoice.tts;

import com.google.common.collect.ImmutableList;
import dev.felnull.ttsvoice.Main;
import dev.felnull.ttsvoice.audio.VoiceAudioPlayerManager;
import dev.felnull.ttsvoice.data.ServerSaveData;
import dev.felnull.ttsvoice.discord.BotLocation;
import dev.felnull.ttsvoice.tts.sayedtext.LiteralSayedText;
import dev.felnull.ttsvoice.tts.sayedtext.SayedText;
import dev.felnull.ttsvoice.tts.tracker.BaseTTSTracker;
import dev.felnull.ttsvoice.tts.tracker.TextMessageTTSTracker;
import dev.felnull.ttsvoice.util.DiscordUtils;
import dev.felnull.ttsvoice.util.URLUtils;
import dev.felnull.ttsvoice.voice.VoiceCategory;
import dev.felnull.ttsvoice.voice.VoiceType;
import dev.felnull.ttsvoice.voice.googletranslate.GoogleTranslateTTSType;
import dev.felnull.ttsvoice.voice.googletranslate.GoogleTranslateVoiceCategory;
import dev.felnull.ttsvoice.voice.reinoare.ReinoareVoiceCategory;
import dev.felnull.ttsvoice.voice.reinoare.cookie.CookieManager;
import dev.felnull.ttsvoice.voice.reinoare.inm.INMManager;
import dev.felnull.ttsvoice.voice.voicetext.VTVoiceCategory;
import dev.felnull.ttsvoice.voice.voicetext.VTVoiceTypes;
import dev.felnull.ttsvoice.voice.vvengine.coeiroink.CIVoiceCategory;
import dev.felnull.ttsvoice.voice.vvengine.coeiroink.CoeiroInkManager;
import dev.felnull.ttsvoice.voice.vvengine.voicevox.VVVoiceCategory;
import dev.felnull.ttsvoice.voice.vvengine.voicevox.VoiceVoxManager;
import net.dv8tion.jda.api.entities.Guild;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.regex.Pattern;

public class TTSManager {
    private static final Logger LOGGER = LogManager.getLogger(TTSManager.class);
    private static final TTSManager INSTANCE = new TTSManager();
    private final Map<BotLocation, Long> ttsChanel = new HashMap<>();
    private final Map<TextMessageTTSTrackerKey, TextMessageTTSTracker> trackers = new HashMap<>();
    private Pattern ignorePattern;

    public static TTSManager getInstance() {
        return INSTANCE;
    }

    public void reconnect(BotLocation bag, long newAudioChannel) {
        long pt = -1;
        synchronized (ttsChanel) {
            if (ttsChanel.containsKey(bag))
                pt = ttsChanel.get(bag);
        }

        disconnect(bag);

        if (pt == -1) {
//            throw new IllegalArgumentException("Not set tts channel");
            return;
        }

        connect(bag, pt, newAudioChannel);
    }

    public void connect(BotLocation bag, long ttsChanelId, long audioChannel) {
        setTTSChanel(bag, ttsChanelId);
        Main.getServerSaveData(bag.guildId()).setLastJoinChannel(bag.botUserId(), new ServerSaveData.TTSEntry(audioChannel, ttsChanelId));
    }

    public void disconnect(BotLocation bag) {
        removeTTSChanel(bag);
        VoiceAudioPlayerManager.getInstance().reloadSchedulers(bag);
        Main.getServerSaveData(bag.guildId()).removeLastJoinChannel(bag.botUserId());
    }

    public long getTTSChanel(BotLocation bag) {
        if (ttsChanel.containsKey(bag)) return ttsChanel.get(bag);
        return -1;
    }

    public long getTTSVoiceChanel(Guild guild) {
        var cv = guild.getAudioManager().getConnectedChannel();
        if (cv == null) return -1;
        return cv.getIdLong();
    }

    public void setTTSChanel(BotLocation botLocation, long chanelId) {
        synchronized (ttsChanel) {
            ttsChanel.put(botLocation, chanelId);
        }
    }

    public void removeTTSChanel(BotLocation botLocation) {
        synchronized (ttsChanel) {
            ttsChanel.remove(botLocation);
        }
    }

    public VoiceType getUserVoiceType(long userId, long guildId) {
        var uvt = Main.getSaveData().getVoiceType(userId, guildId);
        if (uvt != null) return uvt;

        var dvt = getDefaultVoiceType(userId, guildId);
        if (dvt == null)
            throw new RuntimeException("Failed to get default readalond voice type");
        return dvt;
    }

    private VoiceType getMyVoiceType(BotLocation bag) {
        return getUserVoiceType(bag.botUserId(), bag.guildId());
    }

    private VoiceType getDefaultVoiceType(long userId, long guildId) {
    /*    var dvt = getVoiceTypeById("voicevox-2", userId, guildId);
        if (dvt != null)
            return dvt;

        dvt = getVoiceTypeById("google-translate-tts-ja", userId, guildId);
        if (dvt != null)
            return dvt;

        return getVoiceTypeById("voicetext-show", userId, guildId);*/
        return getInitialVoice(userId, guildId);
    }

    private VoiceType getInitialVoice(long userId, long guildId) {
        if (userId == 419032985476530176L)
            return getVoiceTypeById(INMManager.getInstance().getVoice().getId(), userId, guildId);

        var rnd = new Random(userId);
        var types = getVoiceTypes(userId, guildId);
        return types.get(rnd.nextInt(types.size()));
    }

    public VoiceType getVoiceTypeById(String id, long userId, long guildId) {
        return getVoiceTypes(userId, guildId).stream().filter(n -> n.getId().equals(id)).findFirst().orElse(null);
    }

    public void setUserVoceTypes(long userId, VoiceType type) {
        Main.getSaveData().setVoiceType(userId, type);
    }

    public List<VoiceType> getVoiceTypes(long userId, long guildId) {
        ImmutableList.Builder<VoiceType> builder = new ImmutableList.Builder<>();
        var vc = Main.getConfig().voiceConfig();

        if (vc.enableVoiceVox())
            builder.addAll(VoiceVoxManager.getInstance().getSpeakers());
        if (vc.enableCoeiroInk())
            builder.addAll(CoeiroInkManager.getInstance().getSpeakers());
        if (vc.enableVoiceText())
            builder.add(VTVoiceTypes.values());
        if (vc.enableGoogleTranslateTts())
            builder.add(GoogleTranslateTTSType.values());

        if (vc.enableInm()) {
            boolean flg1 = Main.getServerSaveData(guildId).isInmMode(guildId);
            boolean flg2 = !Main.getConfig().inmDenyUser().contains(userId);

            if (flg1 && flg2 && !DiscordUtils.isNonAllowInm(guildId))
                builder.add(INMManager.getInstance().getVoice());
        }

        if (vc.enableCookie()) {
            boolean flg3 = Main.getServerSaveData(guildId).isCookieMode(guildId);
            boolean flg4 = !Main.getConfig().cookieDenyUser().contains(userId);

            if (flg3 && flg4 && !DiscordUtils.isNonAllowCookie(guildId))
                builder.add(CookieManager.getInstance().getVoice());
        }

        return builder.build().stream().filter(VoiceType::isAlive).toList();
    }

    public List<VoiceCategory> getVoiceCategories(long userId, long guildId) {
        var builder = new ImmutableList.Builder<VoiceCategory>()
                .add(VVVoiceCategory.getInstance())
                .add(CIVoiceCategory.getInstance())
                .add(VTVoiceCategory.getInstance())
                .add(GoogleTranslateVoiceCategory.getInstance());

        var flg1 = Main.getServerSaveData(guildId).isInmMode(guildId);
        var flg2 = !Main.getConfig().inmDenyUser().contains(userId);
        var flg3 = Main.getServerSaveData(guildId).isCookieMode(guildId);
        var flg4 = !Main.getConfig().cookieDenyUser().contains(userId);

        if (flg1 && flg2 && !DiscordUtils.isNonAllowInm(guildId) || flg3 && flg4 && !DiscordUtils.isNonAllowCookie(guildId)) {
            builder.add(ReinoareVoiceCategory.getInstance());
        }
        var types = getVoiceTypes(userId, guildId);
        return builder.build().stream().filter(c -> types.stream().anyMatch(t -> t.getCategory().equals(c))).toList();
    }

    public void sayChat(BotLocation botLocation, long userId, String text, long channelId, long messageId) {
        var cs = createSayChat(botLocation, userId, text);
        if (cs == null) return;
        var trackerKey = new TextMessageTTSTrackerKey(botLocation, channelId, messageId);
        var tracker = new TextMessageTTSTracker(botLocation, text, channelId, messageId, () -> {
            synchronized (trackers) {
                trackers.remove(trackerKey);
            }
        });
        synchronized (trackers) {
            trackers.put(trackerKey, tracker);
        }
        sayText(botLocation, cs.getRight(), cs.getLeft(), tracker);
    }

    public Pair<SayedText, VoiceType> createSayChat(BotLocation botLocation, long userId, String text) {
        if (ignorePattern == null) ignorePattern = Pattern.compile(Main.getConfig().ignoreRegex());

        if (ignorePattern.matcher(text).matches()) return null;

        text = DiscordUtils.toCodeBlockSyoryaku(text);
        text = DiscordUtils.replaceMentionToText(botLocation, text);
        text = URLUtils.replaceURLToText(text);

        int pl = text.length();
        var vt = getUserVoiceType(userId, botLocation.guildId());

        int max = vt.getMaxTextLength(botLocation.guildId());
        if (text.length() >= max) text = text.substring(0, max);

        if (pl - text.length() > 0) text += "、以下" + (pl - text.length()) + "文字を省略";

        return Pair.of(new LiteralSayedText(text), vt);
    }

    public void saySystemText(BotLocation bag, SayedText sayedText) {
        sayText(bag, getMyVoiceType(bag), sayedText, null);
    }

    public void sayText(BotLocation bag, VoiceType voiceType, String text) {
        sayText(bag, voiceType, new LiteralSayedText(text), null);
    }

    public void sayText(BotLocation bag, VoiceType voiceType, String text, BaseTTSTracker tracker) {
        sayText(bag, voiceType, new LiteralSayedText(text), tracker);
    }

    public void sayText(BotLocation bag, long userId, String text) {
        sayText(bag, getUserVoiceType(userId, bag.guildId()), text, null);
    }

    public void sayText(BotLocation bag, long userId, String text, BaseTTSTracker tracker) {
        sayText(bag, getUserVoiceType(userId, bag.guildId()), text, tracker);
    }

    public void sayText(BotLocation bag, long userId, SayedText sayedText) {
        sayText(bag, getUserVoiceType(userId, bag.guildId()), sayedText, null);
    }

    public void sayText(BotLocation bag, long userId, SayedText sayedText, BaseTTSTracker tracker) {
        sayText(bag, getUserVoiceType(userId, bag.guildId()), sayedText, tracker);
    }

    public void sayText(BotLocation botLocation, VoiceType voiceType, SayedText sayedText, BaseTTSTracker tracker) {
        if (voiceType == INMManager.getInstance().getVoice() && !Main.getServerSaveData(botLocation.guildId()).isInmMode(botLocation.guildId()))
            return;
        if (voiceType == CookieManager.getInstance().getVoice() && !Main.getServerSaveData(botLocation.guildId()).isCookieMode(botLocation.guildId()))
            return;

        VoiceAudioPlayerManager.getInstance().addTTS(botLocation, new TTSVoiceEntry(new TTSVoice(sayedText, voiceType), UUID.randomUUID(), tracker));
        //var sc = VoiceAudioPlayerManager.getInstance().getScheduler(botLocation);
        /*var q = getTTSQueue(botLocation);
        if (Main.getServerSaveData(botLocation.guildId()).isOverwriteAloud()) {
            q.clear();
            sc.stop();
        }

        q.add(new TTSVoiceEntry(new TTSVoice(sayedText, voiceType), UUID.randomUUID(), tracker));
        if (!sc.isLoadingOrPlaying()) sc.next();*/
    }

    public long getTTSCount() {
        synchronized (ttsChanel) {
            return ttsChanel.keySet().stream().map(n -> n.getGuild().getAudioManager()).filter(n -> n.getConnectedChannel() != null).mapToLong(n -> n.getConnectedChannel().getIdLong()).distinct().count();
        }
    }

    public Map<TextMessageTTSTrackerKey, TextMessageTTSTracker> getTrackers() {
        return trackers;
    }

    public static record TextMessageTTSTrackerKey(BotLocation botLocation, long channelId, long messageId) {
    }
}
