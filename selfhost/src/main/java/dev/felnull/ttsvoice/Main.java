package dev.felnull.ttsvoice;

import dev.felnull.ttsvoice.config.SelfHostConfigManager;
import dev.felnull.ttsvoice.core.TTSVoiceRuntime;
import dev.felnull.ttsvoice.savedata.SelfHostSaveDataManager;

import java.util.Timer;

public class Main {
    public static final Timer TIMER = new Timer(true);
    public static TTSVoiceRuntime RUNTIME;

    public static void main(String[] args) throws Exception {
        RUNTIME = TTSVoiceRuntime.newRuntime(SelfHostConfigManager.getInstance(), SelfHostSaveDataManager.getInstance());
        RUNTIME.execute();
    }
}