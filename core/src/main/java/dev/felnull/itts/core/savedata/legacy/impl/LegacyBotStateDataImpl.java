package dev.felnull.itts.core.savedata.legacy.impl;

import dev.felnull.itts.core.savedata.SaveDataManager;
import dev.felnull.itts.core.savedata.legacy.LegacyBotStateData;
import dev.felnull.itts.core.tts.TTSChannelPair;

/**
 * レガシーBOT状態データの実装
 */
final class LegacyBotStateDataImpl implements LegacyBotStateData {

    /**
     * セーブデータマネージャー
     */
    private final SaveDataManager saveDataManager;

    /**
     * サーバーID
     */
    private final long serverId;

    /**
     * BOTのID
     */
    private final long botId;

    /**
     * コンストラクタ
     *
     * @param saveDataManager saveDataManager
     * @param serverId        serverId
     * @param botId           botId
     */
    LegacyBotStateDataImpl(SaveDataManager saveDataManager, long serverId, long botId) {
        this.saveDataManager = saveDataManager;
        this.serverId = serverId;
        this.botId = botId;
    }

    @Override
    public long getConnectedAudioChannel() {
        TTSChannelPair channelPair = saveDataManager.getRepository().getBotStateData(serverId, botId).getConnectedChannel();

        if (channelPair == null) {
            return -1;
        } else {
            return channelPair.speakAudioChannel();
        }
    }

    @Override
    public void setConnectedAudioChannel(long connectedAudioChannel) {
        TTSChannelPair channelPair = saveDataManager.getRepository().getBotStateData(serverId, botId).getConnectedChannel();

        if (connectedAudioChannel >= 0) {
            if (channelPair == null) {
                channelPair = new TTSChannelPair(connectedAudioChannel, -1);
            } else {
                channelPair = new TTSChannelPair(connectedAudioChannel, channelPair.readTextChannel());
            }
        } else {
            if (channelPair != null) {
                channelPair = new TTSChannelPair(-1, channelPair.readTextChannel());
            }
        }

        saveDataManager.getRepository().getBotStateData(serverId, botId).setConnectedChannel(channelPair);
    }

    @Override
    public long getReadAroundTextChannel() {
        TTSChannelPair channelPair = saveDataManager.getRepository().getBotStateData(serverId, botId).getConnectedChannel();

        if (channelPair == null) {
            return -1;
        } else {
            return channelPair.readTextChannel();
        }
    }

    @Override
    public void setReadAroundTextChannel(long readAroundTextChannel) {
        TTSChannelPair channelPair = saveDataManager.getRepository().getBotStateData(serverId, botId).getConnectedChannel();

        if (readAroundTextChannel >= 0) {
            if (channelPair == null) {
                channelPair = new TTSChannelPair(-1, readAroundTextChannel);
            } else {
                channelPair = new TTSChannelPair(channelPair.speakAudioChannel(), readAroundTextChannel);
            }
        } else {
            if (channelPair != null) {
                channelPair = new TTSChannelPair(channelPair.speakAudioChannel(), -1);
            }
        }

        saveDataManager.getRepository().getBotStateData(serverId, botId).setConnectedChannel(channelPair);
    }
}
