package dev.felnull.itts.core.savedata.repository.impl;

import dev.felnull.itts.core.savedata.dao.BotStateDataRecord;
import dev.felnull.itts.core.savedata.dao.ServerBotKey;
import dev.felnull.itts.core.savedata.dao.TTSChannelKeyPair;
import dev.felnull.itts.core.savedata.repository.BotStateData;
import dev.felnull.itts.core.tts.TTSChannelPair;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.OptionalInt;

/**
 * BOTの状態データの実装
 */
class BotStateDataImpl extends RecordData<ServerBotKey, BotStateDataRecord> implements BotStateData {

    /**
     * サーバーID
     */
    private final long serverId;

    /**
     * BOTのID
     */
    private final long botId;

    BotStateDataImpl(DataRepositoryImpl repository, long serverId, long botId) {
        super(repository);
        this.serverId = serverId;
        this.botId = botId;
    }

    @Override
    protected RecordData<ServerBotKey, BotStateDataRecord>.InitRecordContext getInitRecordContext() {
        int serverKeyId = repository.getServerKeyData().getId(serverId);
        int botKeyId = repository.getBotKeyData().getId(botId);
        ServerBotKey serverBotKey = new ServerBotKey(serverKeyId, botKeyId);

        return new InitRecordContext(dao().botStateDataTable(), serverBotKey, () ->
                new BotStateDataRecord(null, null, null, null));
    }

    @Override
    public @Nullable TTSChannelPair getConnectedChannelPair() {
        return sqlProcReturnable(connection -> {
            Optional<TTSChannelKeyPair> connectedChannel = dao().botStateDataTable().selectConnectedChannelKeyPair(connection, recordId());

            if (connectedChannel.isEmpty()) {
                return null;
            }

            Long speakAudioChannel = repository.getChannelKeyData().getKey(connectedChannel.get().speakAudioChannelKey());
            Long readTextChannel = repository.getChannelKeyData().getKey(connectedChannel.get().readTextChannelKey());

            if (speakAudioChannel != null && readTextChannel != null) {
                return new TTSChannelPair(speakAudioChannel, readTextChannel);
            }

            return null;
        });
    }

    @Override
    public void setConnectedChannelPair(@Nullable TTSChannelPair connectedChannel) {
        sqlProc(connection -> {
            TTSChannelKeyPair ttsChannelKeyPair;
            if (connectedChannel != null) {
                int speakAudioChannelKey = repository.getChannelKeyData().getId(connectedChannel.speakAudioChannel());
                int readTextChannelKey = repository.getChannelKeyData().getId(connectedChannel.readTextChannel());
                ttsChannelKeyPair = new TTSChannelKeyPair(speakAudioChannelKey, readTextChannelKey);
            } else {
                ttsChannelKeyPair = null;
            }
            dao().botStateDataTable().updateConnectedChannelKeyPair(connection, recordId(), ttsChannelKeyPair);
        });
    }

    @Override
    public @Nullable TTSChannelPair getReconnectChannelPair() {
        return sqlProcReturnable(connection -> {
            Optional<TTSChannelKeyPair> reconnectChannel = dao().botStateDataTable().selectReconnectChannelKeyPair(connection, recordId());

            if (reconnectChannel.isEmpty()) {
                return null;
            }

            Long speakAudioChannel = repository.getChannelKeyData().getKey(reconnectChannel.get().speakAudioChannelKey());
            Long readTextChannel = repository.getChannelKeyData().getKey(reconnectChannel.get().readTextChannelKey());

            if (speakAudioChannel != null && readTextChannel != null) {
                return new TTSChannelPair(speakAudioChannel, readTextChannel);
            }

            return null;
        });
    }

    @Override
    public void setReconnectChannelPair(@Nullable TTSChannelPair reconnectChannel) {
        sqlProc(connection -> {
            TTSChannelKeyPair ttsChannelKeyPair;
            if (reconnectChannel != null) {
                int speakAudioChannelKey = repository.getChannelKeyData().getId(reconnectChannel.speakAudioChannel());
                int readTextChannelKey = repository.getChannelKeyData().getId(reconnectChannel.readTextChannel());
                ttsChannelKeyPair = new TTSChannelKeyPair(speakAudioChannelKey, readTextChannelKey);
            } else {
                ttsChannelKeyPair = null;
            }
            dao().botStateDataTable().updateReconnectChannelKeyPair(connection, recordId(), ttsChannelKeyPair);
        });
    }

    @Override
    public @Nullable Long getSpeakAudioChannel() {
        return sqlProcReturnable(connection -> {
            OptionalInt channelKeyId = dao().botStateDataTable().selectSpeakAudioChannel(connection, recordId());
            if (channelKeyId.isPresent()) {
                return repository.getChannelKeyData().getKey(channelKeyId.getAsInt());
            } else {
                return null;
            }
        });
    }

    @Override
    public void setSpeakAudioChannel(@Nullable Long channelId) {
        sqlProc(connection -> {
            OptionalInt channelKey = repository.getChannelKeyData().getIdNullable(channelId);
            dao().botStateDataTable().updateSpeakAudioChannel(connection, recordId(), channelKey.isPresent() ? channelKey.getAsInt() : null);
        });
    }

    @Override
    public @Nullable Long getReadAroundTextChannel() {
        return sqlProcReturnable(connection -> {
            OptionalInt channelKeyId = dao().botStateDataTable().selectReadAroundTextChannel(connection, recordId());
            if (channelKeyId.isPresent()) {
                return repository.getChannelKeyData().getKey(channelKeyId.getAsInt());
            } else {
                return null;
            }
        });
    }

    @Override
    public void setReadAroundTextChannel(@Nullable Long channelId) {
        sqlProc(connection -> {
            OptionalInt channelKey = repository.getChannelKeyData().getIdNullable(channelId);
            dao().botStateDataTable().updateReadAroundTextChannel(connection, recordId(), channelKey.isPresent() ? channelKey.getAsInt() : null);
        });
    }
}
