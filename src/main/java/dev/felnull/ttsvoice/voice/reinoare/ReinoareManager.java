package dev.felnull.ttsvoice.voice.reinoare;

import com.google.gson.Gson;
import dev.felnull.ttsvoice.voice.HasSayVoiceMP3Manager;

import java.util.Random;
import java.util.UUID;

public abstract class ReinoareManager extends HasSayVoiceMP3Manager {
    public static final String INC_URL = "https://www.morimori0317.net/inc-sounds-search";
    public static final Gson GSON = new Gson();
    public static final Random RANDOM = new Random();

    public String getFileURL(UUID uuid) {
        if (uuid == null) return null;
        return INC_URL + "/link/" + uuid;
    }
}
