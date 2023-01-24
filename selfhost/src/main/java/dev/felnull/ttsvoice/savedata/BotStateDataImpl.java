package dev.felnull.ttsvoice.savedata;

import com.google.gson.JsonObject;
import dev.felnull.ttsvoice.core.savedata.BotStateData;
import dev.felnull.ttsvoice.core.util.JsonUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.concurrent.atomic.AtomicLong;

public class BotStateDataImpl extends SaveDataBase implements BotStateData {
    private final long guildId;
    private final AtomicLong connectedAudioChannel = new AtomicLong(INIT_CONNECTED_AUDIO_CHANNEL);
    private final AtomicLong readAroundTextChannel = new AtomicLong(INIT_READ_AROUND_TEXT_CHANNEL);

    protected BotStateDataImpl(long guildId) {
        super(new File(SelfHostSaveDataManager.BOT_STATE_DATA_FOLDER, guildId + ".json"));
        this.guildId = guildId;
    }

    @Override
    public String getName() {
        return "Bot State Data: " + guildId;
    }

    @Override
    protected void loadFromJson(@NotNull JsonObject jo) {
        connectedAudioChannel.set(JsonUtils.getLong(jo, "connected_audio_channel", INIT_CONNECTED_AUDIO_CHANNEL));
        readAroundTextChannel.set(JsonUtils.getLong(jo, "read_around_text_channel", INIT_READ_AROUND_TEXT_CHANNEL));
    }

    @Override
    protected void saveToJson(@NotNull JsonObject jo) {
        jo.addProperty("connected_audio_channel", connectedAudioChannel.get());
        jo.addProperty("read_around_text_channel", readAroundTextChannel.get());
    }

    @Override
    protected int getVersion() {
        return VERSION;
    }

    @Override
    public long getConnectedAudioChannel() {
        return connectedAudioChannel.get();
    }

    @Override
    public void setConnectedAudioChannel(long connectedChannel) {
        long preVal = this.connectedAudioChannel.getAndSet(connectedChannel);
        if (preVal != connectedChannel)
            dirty();
    }

    @Override
    public long getReadAroundTextChannel() {
        return readAroundTextChannel.get();
    }

    @Override
    public void setReadAroundTextChannel(long readAroundTextChannel) {
        long preVal = this.readAroundTextChannel.getAndSet(readAroundTextChannel);
        if (preVal != readAroundTextChannel)
            dirty();
    }
}
