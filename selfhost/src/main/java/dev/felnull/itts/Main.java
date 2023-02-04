package dev.felnull.itts;

import dev.felnull.itts.config.SelfHostConfigManager;
import dev.felnull.itts.core.TTSVoiceRuntime;
import dev.felnull.itts.savedata.SelfHostSaveDataManager;

public class Main {
    public static TTSVoiceRuntime RUNTIME;

    public static void main(String[] args) throws Exception {
        RUNTIME = TTSVoiceRuntime.newRuntime(SelfHostConfigManager.getInstance(), SelfHostSaveDataManager.getInstance(), null/*GlobalCacheTest::new*/);
        RUNTIME.execute();
    }
}