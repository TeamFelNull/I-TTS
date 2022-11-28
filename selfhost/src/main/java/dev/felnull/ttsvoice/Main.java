package dev.felnull.ttsvoice;

import dev.felnull.ttsvoice.core.TTSVoiceRuntime;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static TTSVoiceRuntime RUNTIME;

    public static void main(String[] args) throws Exception {
        String token = Files.readString(Paths.get("./token.txt"));

        RUNTIME = TTSVoiceRuntime.create(token);
        RUNTIME.run();
    }
}