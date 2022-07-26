package dev.felnull.ttsvoice.voice.reinoare;

import dev.felnull.ttsvoice.voice.VoiceEngine;
import dev.felnull.ttsvoice.voice.googletranslate.GoogleTranslateVoiceEngine;

public class ReinoareVoiceEngine implements VoiceEngine {
    @Override
    public String getTitle() {
        return "例のアレ";
    }

    @Override
    public String getId() {
        return "reinoare-";
    }

    public static  ReinoareVoiceEngine getInstance(){
        return new ReinoareVoiceEngine();
    }
}
