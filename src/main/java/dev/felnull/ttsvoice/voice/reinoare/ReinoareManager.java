package dev.felnull.ttsvoice.voice.reinoare;

import com.google.gson.Gson;
import dev.felnull.fnjl.util.FNDataUtil;
import dev.felnull.ttsvoice.voice.reinoare.inm.INMManager;
import dev.felnull.ttsvoice.voice.reinoare.inm.INMVoiceType;

import java.io.InputStream;
import java.util.Random;
import java.util.UUID;

public abstract class ReinoareManager {
    public static final String INC_URL = "https://www.morimori0317.net/inc-sounds-search";
    public static final Gson GSON = new Gson();
    public static final Random RANDOM = new Random();

    public String getFileURL(UUID uuid) {
        if (uuid == null) return null;
        return INC_URL + "/link/" + uuid;
    }

    public InputStream getJoinSound() {
        return getMP3("join");
    }

    public InputStream getMoveFromSound() {
        return getMP3("move_from");
    }

    public InputStream getForceMoveFromSound() {
        return getMP3("f_move_from");
    }

    public InputStream getLeaveSound() {
        return getMP3("leave");
    }

    public InputStream getForceLeaveSound() {
        return getMP3("f_leave");
    }

    public InputStream getMoveToSound() {
        return getMP3("move_to");
    }

    public InputStream getForceMoveToSound() {
        return getMP3("f_move_to");
    }

    public InputStream getMP3(String path, String name) {
        int num = new Random().nextInt(countFiles(path, name)) + 1;
        return FNDataUtil.resourceExtractor(INMManager.class, path + name + num + ".mp3");
    }

    abstract public InputStream getMP3(String name);

    public static int countFiles(String path, String word) {
        return (int) FNDataUtil.resourceExtractEntry(INMManager.class, path).stream().filter(r -> !r.isDirectory() && r.getName().startsWith(word)).count();
    }
}
