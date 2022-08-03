package dev.felnull.ttsvoice.tts.sayedtext;

public record LiteralSayedText(String text) implements SayedText {
    @Override
    public String getSayVoiceText() {
        return text;
    }
}
