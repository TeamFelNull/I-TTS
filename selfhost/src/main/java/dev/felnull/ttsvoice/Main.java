package dev.felnull.ttsvoice;

import dev.felnull.ttsvoice.core.TTSVoiceRuntime;

public class Main {
    public static TTSVoiceRuntime RUNTIME;

    public static void main(String[] args) {
        RUNTIME = TTSVoiceRuntime.create("");
        RUNTIME.run();
    }
}