package dev.felnull.ttsvoice.voice.reinoare;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.felnull.fnjl.util.FNURLUtil;
import dev.felnull.ttsvoice.Main;
import dev.felnull.ttsvoice.voice.HasSayVoiceMP3Manager;
import dev.felnull.ttsvoice.voice.SimpleAliveChecker;
import dev.felnull.ttsvoice.voice.VoiceType;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public abstract class ReinoareManager extends HasSayVoiceMP3Manager {
    public static final String INC_URL = "https://www.morimori0317.net/inc-sounds-search";
    public static final Gson GSON = new Gson();
    public static final Random RANDOM = new Random();
    public static final SimpleAliveChecker ALIVE_CHECKER = new SimpleAliveChecker(() -> {
        var vc = Main.getConfig().voiceConfig();
        return vc.enableInm() || vc.enableCookie();
    }, () -> {
        try {
            var rep = FNURLUtil.getResponse(new URL(ReinoareManager.INC_URL + "/search"));
            GSON.fromJson(rep, JsonObject.class);
            return true;
        } catch (Exception ex) {
            return false;
        }
    });

    public List<ReinoareEntry> search(String text) throws URISyntaxException, IOException {
        return search(text, 150);
    }

    abstract public List<ReinoareEntry> search(String text, int max) throws URISyntaxException, IOException;

    abstract public List<ReinoareEntry> sort(List<ReinoareEntry> entries);

    public String getFileURL(UUID uuid) {
        if (uuid == null) return null;
        return INC_URL + "/link/" + uuid;
    }

    abstract public VoiceType getVoice();

    public boolean isAlive() {
        return ALIVE_CHECKER.isAlive();
    }

    abstract public boolean isEnable(long guildId);
}
