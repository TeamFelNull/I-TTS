package dev.felnull.itts;

import dev.felnull.itts.core.ITTSRuntime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    public static final Logger LOGGER = LogManager.getLogger(Main.class);
    public static ITTSRuntime RUNTIME;

    public static void main(String[] args) throws Exception {
        RUNTIME = ITTSRuntime.newRuntime(new SelfHostITTSRuntimeContext());
        RUNTIME.execute();
    }
}