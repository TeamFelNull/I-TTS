package dev.felnull.ttsvoice.core;

public class TTSVoiceRuntime {
    private TTSVoiceRuntime() {
    }

    public static TTSVoiceRuntime create() {
        return new TTSVoiceRuntime();
    }

    public void run() {
        System.out.println("IKISUGI");
    }
}
