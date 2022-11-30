package dev.felnull.ttsvoice;

import dev.felnull.ttsvoice.config.ConfigAccessImpl;
import dev.felnull.ttsvoice.core.TTSVoiceRuntime;

public class Main {
    public static TTSVoiceRuntime RUNTIME;

    public static void main(String[] args) throws Exception {
        RUNTIME = TTSVoiceRuntime.newRuntime(new ConfigAccessImpl());
        RUNTIME.run();
    }
}