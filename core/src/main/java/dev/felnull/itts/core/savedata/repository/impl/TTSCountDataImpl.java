package dev.felnull.itts.core.savedata.repository.impl;

import dev.felnull.itts.core.savedata.dao.TTSCountRecord;
import dev.felnull.itts.core.savedata.repository.TTSCountData;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 読み上げ文字数集計データの実装
 */
class TTSCountDataImpl extends SaveDataBase implements TTSCountData {

    /**
     * BOT ID
     */
    private final long botId;

    /**
     * サーバーID nullの場合はBOT全体合計
     */
    @Nullable
    private final Long serverId;

    /**
     * 集計日
     */
    private final LocalDate date;

    TTSCountDataImpl(DataRepositoryImpl repository, long botId, @Nullable Long serverId, LocalDate date) {
        super(repository);
        this.botId = botId;
        this.serverId = serverId;
        this.date = date;
    }

    @Override
    public void addCount(long charDelta, long messageDelta) {
        sqlProc(connection -> {
            int botKeyId = repository.getBotKeyData().getId(botId);
            Integer serverKeyId = serverId == null ? null : repository.getServerKeyData().getId(serverId);
            dao().ttsCountTable().incrementCount(connection, botKeyId, serverKeyId, date, charDelta, messageDelta);
        });
    }

    @Override
    public long getCharCount() {
        return sqlProcReturnable(connection -> {
            int botKeyId = repository.getBotKeyData().getId(botId);
            Integer serverKeyId = serverId == null ? null : repository.getServerKeyData().getId(serverId);
            Optional<TTSCountRecord> rec = dao().ttsCountTable().getCount(connection, botKeyId, serverKeyId, date);
            return rec.map(TTSCountRecord::charCount).orElse(0L);
        });
    }

    @Override
    public long getMessageCount() {
        return sqlProcReturnable(connection -> {
            int botKeyId = repository.getBotKeyData().getId(botId);
            Integer serverKeyId = serverId == null ? null : repository.getServerKeyData().getId(serverId);
            Optional<TTSCountRecord> rec = dao().ttsCountTable().getCount(connection, botKeyId, serverKeyId, date);
            return rec.map(TTSCountRecord::messageCount).orElse(0L);
        });
    }
}
