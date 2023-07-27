package dev.felnull.itts.core.savedata;

public interface BotStateData {
    int VERSION = 0;
    long INIT_CONNECTED_AUDIO_CHANNEL = -1;
    long INIT_READ_AROUND_TEXT_CHANNEL = -1;

    long getConnectedAudioChannel();

    void setConnectedAudioChannel(long connectedAudioChannel);

    long getReadAroundTextChannel();

    void setReadAroundTextChannel(long readAroundTextChannel);
}
