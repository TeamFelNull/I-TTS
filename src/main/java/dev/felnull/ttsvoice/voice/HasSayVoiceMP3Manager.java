package dev.felnull.ttsvoice.voice;

import dev.felnull.fnjl.util.FNDataUtil;
import dev.felnull.ttsvoice.Main;
import dev.felnull.ttsvoice.voice.reinoare.inm.INMManager;

import java.io.InputStream;
import java.util.Random;

public abstract class HasSayVoiceMP3Manager {

    public InputStream getConnectSound() {
        return getMP3("connect");
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
        return FNDataUtil.resourceExtractor(Main.class, path + name + num + ".mp3");
    }

    abstract public InputStream getMP3(String name);

    public static int countFiles(String path, String word) {
        return (int) FNDataUtil.resourceExtractEntry(Main.class, path).stream().filter(r -> !r.isDirectory() && r.getName().startsWith(word)).count();
    }
}
