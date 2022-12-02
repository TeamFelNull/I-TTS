package dev.felnull.ttsvoice.core.savedata;

public interface BotServerData {
    int VERSION = 0;
    long DEFAULT_CONNECTED_CHANNEL = -1;

    long getConnectedChannel();

    void setConnectedChannel(long connectedChannel);
}
