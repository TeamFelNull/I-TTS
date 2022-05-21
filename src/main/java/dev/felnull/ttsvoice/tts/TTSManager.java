package dev.felnull.ttsvoice.tts;

import com.google.common.collect.ImmutableList;
import dev.felnull.ttsvoice.Main;
import dev.felnull.ttsvoice.audio.VoiceAudioPlayerManager;
import dev.felnull.ttsvoice.voice.inm.INMManager;
import dev.felnull.ttsvoice.util.DiscordUtil;
import dev.felnull.ttsvoice.util.URLUtil;
import dev.felnull.ttsvoice.voice.voicetext.VTVoiceTypes;
import dev.felnull.ttsvoice.voice.voicevox.VoiceVoxManager;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class TTSManager {
    private static final Logger LOGGER = LogManager.getLogger(TTSManager.class);
    private static final TTSManager INSTANCE = new TTSManager();
    private static final File CASH_FOLDER = new File("./cash");
    private final Map<TTSVoice, VoiceCashData> VOICE_CASH = new HashMap<>();
    private final Map<Long, Long> TTS_CHANEL = new HashMap<>();
    private final Map<Long, Queue<TTSVoice>> TTS_QUEUE = new HashMap<>();

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

    public long getTTSChanel(long guildId) {
        if (TTS_CHANEL.containsKey(guildId))
            return TTS_CHANEL.get(guildId);
        return -1;
    }

    public Queue<TTSVoice> getTTSQueue(long guildId) {
        return TTS_QUEUE.computeIfAbsent(guildId, n -> new LinkedBlockingQueue<>());
    }

    public void setTTSChanel(long guildId, long chanelId) {
        TTS_CHANEL.put(guildId, chanelId);
    }

    public void removeTTSChanel(long guildId) {
        TTS_CHANEL.remove(guildId);
        TTS_QUEUE.remove(guildId);
    }

    public IVoiceType getUserVoiceType(long userId) {
        var uvt = Main.SAVE_DATA.getVoiceType(userId);
        if (uvt != null)
            return uvt;
        return getVoiceTypeById("voicevox-2");
    }

    public IVoiceType getVoiceTypeById(String id) {
        return getVoiceTypes().stream().filter(n -> n.getId().equals(id)).findFirst().orElse(null);
    }

    public void setUserVoceTypes(long userId, IVoiceType type) {
        Main.SAVE_DATA.setVoiceType(userId, type);
    }

    public List<IVoiceType> getVoiceTypes() {
        ImmutableList.Builder<IVoiceType> builder = new ImmutableList.Builder<>();
        builder.addAll(VoiceVoxManager.getInstance().getSpeakers());
        builder.add(VTVoiceTypes.values());
        builder.add(INMManager.getInstance().getVoice());
        return builder.build();
    }

    public void onChat(long guildId, long userId, String text) {
        text = DiscordUtil.replaceMentionToText(Main.JDA.getGuildById(guildId), text);
        text = URLUtil.replaceURLToText(text);
        int pl = text.length();

        if (text.length() >= 200)
            text = text.substring(0, 200);

        if (pl - text.length() > 0)
            text += "、以下" + (pl - text.length()) + "文字を省略";

        onText(guildId, getUserVoiceType(userId), text);
    }

    public void onText(long guildId, IVoiceType voiceType, String text) {
        text = voiceType.replace(text);
        getTTSQueue(guildId).add(new TTSVoice(text, voiceType));
        var sc = VoiceAudioPlayerManager.getInstance().getScheduler(guildId);
        if (!sc.isLoadingOrPlaying())
            sc.next();
    }

    public Map<TTSVoice, VoiceCashData> getVoiceCash() {
        return VOICE_CASH;
    }

    public File getVoiceFile(TTSVoice voice) {
        synchronized (VOICE_CASH) {
            var c = getVoiceCashData(voice);
            if (c != null) {
                if (c.isInvalid())
                    return null;
                return getCashFile(c.getId());
            }
            byte[] data;
            try {
                data = voice.voiceType().getSound(voice.text());
                if (data == null) {
                    VOICE_CASH.put(voice, new VoiceCashData(null));
                    return null;
                }
            } catch (Exception ex) {
                LOGGER.error("Failed to get audio data", ex);
                VOICE_CASH.put(voice, new VoiceCashData(null));
                return null;
            }
            try {
                var uuid = UUID.randomUUID();
                var file = CASH_FOLDER.toPath().resolve(uuid.toString());
                Files.write(file, data);
                VOICE_CASH.put(voice, new VoiceCashData(uuid));
                return file.toFile();
            } catch (IOException ex) {
                LOGGER.error("Failed to write audio data cash", ex);
                VOICE_CASH.put(voice, new VoiceCashData(null));
                return null;
            }
        }
    }
}
