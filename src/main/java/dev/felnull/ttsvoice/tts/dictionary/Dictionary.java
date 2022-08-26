package dev.felnull.ttsvoice.tts.dictionary;

import java.util.Map;

public interface Dictionary {
    String replace(String text);

    Map<String, String> getEntryShowTexts();
}
