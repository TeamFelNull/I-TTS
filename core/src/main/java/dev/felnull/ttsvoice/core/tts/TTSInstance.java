package dev.felnull.ttsvoice.core.tts;

public class TTSInstance {
    private final long channel;

    public TTSInstance(long channel) {
        this.channel = channel;
    }

    public long getChannel() {
        return channel;
    }

    public void destroy() {

    }

    public void sayChat(long userId, String text) {

    }

    public void sayText(String text) {

    }
}
