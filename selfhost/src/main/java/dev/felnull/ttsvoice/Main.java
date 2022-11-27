package dev.felnull.ttsvoice;

import dev.felnull.ttsvoice.core.TTSVoiceRuntime;

public class Main {
    public static void main(String[] args) {
        TTSVoiceRuntime ttsVoiceRuntime = TTSVoiceRuntime.create();
        ttsVoiceRuntime.run();
    }
}