package dev.felnull.ttsvoice.tts;

import com.google.common.collect.ImmutableList;
import dev.felnull.ttsvoice.Main;
import dev.felnull.ttsvoice.audio.VoiceAudioPlayerManager;
import dev.felnull.ttsvoice.tts.sayvoice.ISayVoice;
import dev.felnull.ttsvoice.tts.sayvoice.LiteralSayVoice;
import dev.felnull.ttsvoice.util.DiscordUtils;
import dev.felnull.ttsvoice.util.URLUtils;
import dev.felnull.ttsvoice.voice.VoiceType;
import dev.felnull.ttsvoice.voice.googletranslate.GoogleTranslateTTSType;
import dev.felnull.ttsvoice.voice.inm.INMManager;
import dev.felnull.ttsvoice.voice.voicetext.VTVoiceTypes;
import dev.felnull.ttsvoice.voice.voicevox.VoiceVoxManager;
import net.dv8tion.jda.api.entities.Guild;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.regex.Pattern;

public class TTSManager {
    private static final Logger LOGGER = LogManager.getLogger(TTSManager.class);
    private static final TTSManager INSTANCE = new TTSManager();
    private final Map<BotAndGuild, Long> TTS_CHANEL = new HashMap<>();
    private final Map<BotAndGuild, LinkedList<TTSVoiceEntry>> TTS_QUEUE = new HashMap<>();
    private Pattern ignorePattern;

    public static TTSManager getInstance() {
        return INSTANCE;
    }

    public void reconnect(BotAndGuild bag) {
        long pt;
        synchronized (TTS_CHANEL) {
            if (TTS_CHANEL.containsKey(bag))
                pt = TTS_CHANEL.get(bag);
            else
                throw new IllegalArgumentException("Not set tts channel");
        }
        disconnect(bag);
        connect(bag, pt);
    }

    public void connect(BotAndGuild bag, long ttsChanelId) {
        setTTSChanel(bag, ttsChanelId);
    }

    public void disconnect(BotAndGuild bag) {
        removeTTSChanel(bag);
        VoiceAudioPlayerManager.getInstance().clearSchedulers(bag);
    }

    public long getTTSChanel(BotAndGuild bag) {
        if (TTS_CHANEL.containsKey(bag))
            return TTS_CHANEL.get(bag);
        return -1;
    }

    public long getTTSVoiceChanel(Guild guild) {
        var cv = guild.getAudioManager().getConnectedChannel();
        if (cv == null) return -1;
        return cv.getIdLong();
    }

    public LinkedList<TTSVoiceEntry> getTTSQueue(BotAndGuild bag) {
        synchronized (TTS_QUEUE) {
            return TTS_QUEUE.computeIfAbsent(bag, n -> new LinkedList<>());
        }
    }

    public void setTTSChanel(BotAndGuild bag, long chanelId) {
        synchronized (TTS_CHANEL) {
            TTS_CHANEL.put(bag, chanelId);
        }
    }

    public void removeTTSChanel(BotAndGuild bag) {
        synchronized (TTS_CHANEL) {
            TTS_CHANEL.remove(bag);
        }
        synchronized (TTS_QUEUE) {
            TTS_QUEUE.remove(bag);
        }
    }

    public VoiceType getUserVoiceType(long userId, long guildId) {
        var uvt = Main.SAVE_DATA.getVoiceType(userId, guildId);
        if (uvt != null)
            return uvt;
        return getVoiceTypeById("voicevox-2", userId, guildId);
    }

    public VoiceType getVoiceTypeById(String id, long userId, long guildId) {
        return getVoiceTypes(userId, guildId).stream().filter(n -> n.getId().equals(id)).findFirst().orElse(null);
    }

    public void setUserVoceTypes(long userId, VoiceType type) {
        Main.SAVE_DATA.setVoiceType(userId, type);
    }

    public List<VoiceType> getVoiceTypes(long userId, long guildId) {
        ImmutableList.Builder<VoiceType> builder = new ImmutableList.Builder<>();
        builder.addAll(VoiceVoxManager.getInstance().getSpeakers());
        builder.add(VTVoiceTypes.values());
        builder.add(GoogleTranslateTTSType.values());

        boolean flg1 = Main.getServerConfig(guildId).isInmMode(guildId);
        boolean flg2 = !Main.CONFIG.inmDenyUser().contains(userId);

        if (flg1 && flg2 && !DiscordUtils.isNonAllowInm(guildId))
            builder.add(INMManager.getInstance().getVoice());

        return builder.build();
    }

    public void sayChat(BotAndGuild bag, long userId, String text) {
        if (ignorePattern == null)
            ignorePattern = Pattern.compile(Main.CONFIG.ignoreRegex());

        if (ignorePattern.matcher(text).matches())
            return;

        text = DiscordUtils.toCodeBlockSyoryaku(text);
        text = DiscordUtils.replaceMentionToText(bag.botNumber(), bag.getGuild(), text);
        text = URLUtils.replaceURLToText(text);

        int pl = text.length();
        var vt = getUserVoiceType(userId, bag.guildId());

        int max = vt.getMaxTextLength(bag.guildId());
        if (text.length() >= max)
            text = text.substring(0, max);

        if (pl - text.length() > 0)
            text += "、以下" + (pl - text.length()) + "文字を省略";

        sayText(bag, vt, text);
    }

    public void sayText(BotAndGuild bag, VoiceType voiceType, String text) {
        sayText(bag, voiceType, new LiteralSayVoice(text));
    }

    public void sayText(BotAndGuild bag, long userId, String text) {
        sayText(bag, getUserVoiceType(userId, bag.guildId()), text);
    }

    public void sayText(BotAndGuild bag, long userId, ISayVoice sayVoice) {
        sayText(bag, getUserVoiceType(userId, bag.guildId()), sayVoice);
    }

    public void sayText(BotAndGuild bag, VoiceType voiceType, ISayVoice sayVoice) {
        var sc = VoiceAudioPlayerManager.getInstance().getScheduler(bag);
        var q = getTTSQueue(bag);
        if (Main.getServerConfig(bag.guildId()).isOverwriteAloud()) {
            q.clear();
            sc.stop();
        }

        q.add(new TTSVoiceEntry(new TTSVoice(sayVoice, voiceType), UUID.randomUUID()));
        if (!sc.isLoadingOrPlaying())
            sc.next();
    }

    public long getTTSCount() {
        synchronized (TTS_CHANEL) {
            return TTS_CHANEL.keySet().stream().map(n -> n.getGuild().getAudioManager()).filter(n -> n.getConnectedChannel() != null).mapToLong(n -> n.getConnectedChannel().getIdLong()).distinct().count();
        }
    }
}
