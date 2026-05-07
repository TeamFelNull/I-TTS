package dev.felnull.itts.core.savedata.repository.impl;

import dev.felnull.itts.core.savedata.dao.DAO;
import dev.felnull.itts.core.savedata.dao.TTSCountRecord;
import dev.felnull.itts.core.savedata.repository.TTSCountData;

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
     * サーバーID 0の場合はBOT全体合計
     */
    private final long serverId;

    /**
     * 集計日
     */
    private final LocalDate date;

    TTSCountDataImpl(DataRepositoryImpl repository, long botId, long serverId, LocalDate date) {
        super(repository);
        this.botId = botId;
        this.serverId = serverId;
        this.date = date;
    }

    private int resolveServerKeyId() {
        if (serverId == 0L) {
            return DAO.TTSCountTable.GLOBAL_SERVER_KEY_ID;
        }
        return repository.getServerKeyData().getId(serverId);
    }

    @Override
    public void addCount(long charDelta, long messageDelta) {
        sqlProc(connection -> {
            int botKeyId = repository.getBotKeyData().getId(botId);
            int serverKeyId = resolveServerKeyId();
            dao().ttsCountTable().incrementCount(connection, botKeyId, serverKeyId, date, charDelta, messageDelta);
        });
    }

    @Override
    public Optional<TTSCountRecord> getRecord() {
        return sqlProcReturnable(connection -> {
            int botKeyId = repository.getBotKeyData().getId(botId);
            int serverKeyId = resolveServerKeyId();
            return dao().ttsCountTable().getCount(connection, botKeyId, serverKeyId, date);
        });
    }
}
