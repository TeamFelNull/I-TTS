package dev.felnull.ttsvoice.tts;

import com.google.common.collect.ImmutableList;
import dev.felnull.fnjl.util.FNDataUtil;
import dev.felnull.ttsvoice.Main;
import dev.felnull.ttsvoice.audio.VoiceAudioPlayerManager;
import dev.felnull.ttsvoice.tts.sayvoice.ISayVoice;
import dev.felnull.ttsvoice.tts.sayvoice.LiteralSayVoice;
import dev.felnull.ttsvoice.util.DiscordUtils;
import dev.felnull.ttsvoice.util.URLUtils;
import dev.felnull.ttsvoice.voice.googletranslate.GoogleTranslateTTSType;
import dev.felnull.ttsvoice.voice.inm.INMManager;
import dev.felnull.ttsvoice.voice.voicetext.VTVoiceTypes;
import dev.felnull.ttsvoice.voice.voicevox.VoiceVoxManager;
import net.dv8tion.jda.api.entities.Guild;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Pattern;

public class TTSManager {
    private static final Logger LOGGER = LogManager.getLogger(TTSManager.class);
    private static final TTSManager INSTANCE = new TTSManager();
    private static final File CASH_FOLDER = new File("./cash");
    private final Map<TTSVoice, VoiceCashData> VOICE_CASH = new HashMap<>();
    private final Map<BotAndGuild, Long> TTS_CHANEL = new HashMap<>();
    private final Map<BotAndGuild, Queue<TTSVoice>> TTS_QUEUE = new HashMap<>();
    private Pattern ignorePattern;

    public static TTSManager getInstance() {
        return INSTANCE;
    }

    public void init() throws IOException {
        FileUtils.deleteDirectory(CASH_FOLDER);
        CASH_FOLDER.mkdirs();

        Timer timer = new Timer();
        TimerTask cashManageTask = new TimerTask() {
            public void run() {
                clearCash();
            }
        };
        timer.scheduleAtFixedRate(cashManageTask, 0, 60 * 1000);
    }

    private void clearCash() {
        synchronized (VOICE_CASH) {
            List<TTSVoice> rm = new ArrayList<>();
            for (Map.Entry<TTSVoice, VoiceCashData> entry : VOICE_CASH.entrySet()) {
                if (entry.getValue().timeOut()) {
                    rm.add(entry.getKey());
                    if (!entry.getValue().isInvalid()) {
                        var fil = getCashFile(entry.getValue().getId());
                        if (fil.exists())
                            fil.delete();
                    }
                }
            }
            rm.forEach(VOICE_CASH::remove);
        }
    }

    public File getCashFile(UUID id) {
        return new File(CASH_FOLDER, id.toString());
    }

    public VoiceCashData getVoiceCashData(TTSVoice voice) {
        var data = VOICE_CASH.get(voice);
        if (data != null)
            data.update();
        return data;
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

    public Queue<TTSVoice> getTTSQueue(BotAndGuild bag) {
        return TTS_QUEUE.computeIfAbsent(bag, n -> new LinkedBlockingQueue<>());
    }

    public void setTTSChanel(BotAndGuild bag, long chanelId) {
        TTS_CHANEL.put(bag, chanelId);
    }

    public void removeTTSChanel(BotAndGuild bag) {
        TTS_CHANEL.remove(bag);
        TTS_QUEUE.remove(bag);
    }

    public IVoiceType getUserVoiceType(long userId, long guildId) {
        var uvt = Main.SAVE_DATA.getVoiceType(userId, guildId);
        if (uvt != null)
            return uvt;
        return getVoiceTypeById("voicevox-2", userId, guildId);
    }

    public IVoiceType getVoiceTypeById(String id, long userId, long guildId) {
        return getVoiceTypes(userId, guildId).stream().filter(n -> n.getId().equals(id)).findFirst().orElse(null);
    }

    public void setUserVoceTypes(long userId, IVoiceType type) {
        Main.SAVE_DATA.setVoiceType(userId, type);
    }

    public List<IVoiceType> getVoiceTypes(long userId, long guildId) {
        ImmutableList.Builder<IVoiceType> builder = new ImmutableList.Builder<>();
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

    public void sayText(BotAndGuild bag, IVoiceType voiceType, String text) {
        sayText(bag, voiceType, new LiteralSayVoice(text));
    }

    public void sayText(BotAndGuild bag, long userId, String text) {
        sayText(bag, getUserVoiceType(userId, bag.guildId()), text);
    }

    public void sayText(BotAndGuild bag, long userId, ISayVoice sayVoice) {
        sayText(bag, getUserVoiceType(userId, bag.guildId()), sayVoice);
    }

    public void sayText(BotAndGuild bag, IVoiceType voiceType, ISayVoice sayVoice) {
        var sc = VoiceAudioPlayerManager.getInstance().getScheduler(bag);
        var q = getTTSQueue(bag);
        if (Main.getServerConfig(bag.guildId()).isOverwriteAloud()) {
            q.clear();
            sc.stop();
        }

        q.add(new TTSVoice(sayVoice, voiceType));
        if (!sc.isLoadingOrPlaying())
            sc.next();
    }

    public Map<TTSVoice, VoiceCashData> getVoiceCash() {
        return VOICE_CASH;
    }

    public File getVoiceFile(TTSVoice voice) {
        synchronized (VOICE_CASH) {
            boolean cached = voice.voiceType().isCached(voice.sayVoice());
            var c = getVoiceCashData(voice);
            if (c != null) {
                if (cached) {
                    if (c.isInvalid())
                        return null;
                    return getCashFile(c.getId());
                } else {
                    VOICE_CASH.remove(voice);
                    var fil = getCashFile(c.getId());
                    if (fil.exists())
                        fil.delete();
                }
            }
            InputStream voiceStream;
            try {
                voiceStream = voice.voiceType().getSayVoiceSound(voice.sayVoice());
                if (voiceStream == null) {
                    VOICE_CASH.put(voice, new VoiceCashData(null));
                    return null;
                }
            } catch (Exception ex) {
                //     if (voice.voiceType() != INMManager.getInstance().getVoice())
                LOGGER.error("Failed to get audio data", ex);
                VOICE_CASH.put(voice, new VoiceCashData(null));
                return null;
            }
            try {
                var uuid = UUID.randomUUID();
                var file = CASH_FOLDER.toPath().resolve(uuid.toString());
                FNDataUtil.bufInputToOutput(voiceStream, new FileOutputStream(file.toFile()));
                VOICE_CASH.put(voice, new VoiceCashData(uuid));
                return file.toFile();
            } catch (IOException ex) {
                LOGGER.error("Failed to write audio data cash", ex);
                VOICE_CASH.put(voice, new VoiceCashData(null));
                return null;
            }
        }
    }

    public long getTTSCount() {
        return TTS_CHANEL.keySet().stream().map(n -> n.getGuild().getAudioManager()).filter(n -> n.getConnectedChannel() != null).mapToLong(n -> n.getConnectedChannel().getIdLong()).distinct().count();
    }
}
