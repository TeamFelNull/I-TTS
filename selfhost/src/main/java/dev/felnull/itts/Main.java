package dev.felnull.itts;

import dev.felnull.itts.config.SelfHostConfigManager;
import dev.felnull.itts.core.ITTSRuntime;
import dev.felnull.itts.savedata.SelfHostSaveDataManager;

public class Main {
    public static ITTSRuntime RUNTIME;

    public static void main(String[] args) throws Exception {
        RUNTIME = ITTSRuntime.newRuntime(SelfHostConfigManager.getInstance(), SelfHostSaveDataManager.getInstance(), null/*GlobalCacheTest::new*/);
        RUNTIME.execute();
    }
}