package dev.felnull.ttsvoice.tts.sayvoice;

public class LiteralSayVoice implements ISayVoice {
    private final String text;

    public LiteralSayVoice(String text) {
        this.text = text;
    }

    @Override
    public String getSayVoiceText() {
        return text;
    }
}
