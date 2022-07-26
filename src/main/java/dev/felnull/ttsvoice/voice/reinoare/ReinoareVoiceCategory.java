package dev.felnull.ttsvoice.voice.reinoare;

import dev.felnull.ttsvoice.voice.VoiceCategory;

public class ReinoareVoiceCategory implements VoiceCategory {
    @Override
    public String getTitle() {
        return "例のアレ";
    }

    @Override
    public String getId() {
        return "reinoare-";
    }

    public static ReinoareVoiceCategory getInstance(){
        return new ReinoareVoiceCategory();
    }
}
