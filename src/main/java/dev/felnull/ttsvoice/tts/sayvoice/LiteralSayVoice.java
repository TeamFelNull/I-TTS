package dev.felnull.ttsvoice.tts.sayvoice;

public record LiteralSayVoice(String text) implements ISayVoice {
    @Override
    public String getSayVoiceText() {
        return text;
    }
}
