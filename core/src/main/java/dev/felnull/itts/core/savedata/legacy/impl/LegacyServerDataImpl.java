package dev.felnull.itts.core.savedata.legacy.impl;

import dev.felnull.itts.core.savedata.SaveDataManager;
import dev.felnull.itts.core.savedata.legacy.LegacyServerData;
import org.jetbrains.annotations.Nullable;

/**
 * レガシーサーバーデータの実装
 */
final class LegacyServerDataImpl implements LegacyServerData {

    /**
     * セーブデータマネージャー
     */
    private final SaveDataManager saveDataManager;

    /**
     * サーバーID
     */
    private final long serverId;

    /**
     * コンストラクタ
     *
     * @param saveDataManager セーブデータマネージャー
     * @param serverId        サーバーID
     */
    LegacyServerDataImpl(SaveDataManager saveDataManager, long serverId) {
        this.saveDataManager = saveDataManager;
        this.serverId = serverId;
    }

    @Override
    public @Nullable String getDefaultVoiceType() {
        return saveDataManager.getRepository().getServerData(serverId).getDefaultVoiceType();
    }

    @Override
    public void setDefaultVoiceType(@Nullable String voiceType) {
        saveDataManager.getRepository().getServerData(serverId).setDefaultVoiceType(voiceType);
    }

    @Override
    public @Nullable String getIgnoreRegex() {
        return saveDataManager.getRepository().getServerData(serverId).getIgnoreRegex();
    }

    @Override
    public void setIgnoreRegex(@Nullable String ignoreRegex) {
        saveDataManager.getRepository().getServerData(serverId).setIgnoreRegex(ignoreRegex);
    }

    @Override
    public boolean isNeedJoin() {
        return saveDataManager.getRepository().getServerData(serverId).isNeedJoin();
    }

    @Override
    public void setNeedJoin(boolean needJoin) {
        saveDataManager.getRepository().getServerData(serverId).setNeedJoin(needJoin);
    }

    @Override
    public boolean isOverwriteAloud() {
        return saveDataManager.getRepository().getServerData(serverId).isOverwriteAloud();
    }

    @Override
    public void setOverwriteAloud(boolean overwriteAloud) {
        saveDataManager.getRepository().getServerData(serverId).setOverwriteAloud(overwriteAloud);
    }

    @Override
    public boolean isNotifyMove() {
        return saveDataManager.getRepository().getServerData(serverId).isNotifyMove();
    }

    @Override
    public void setNotifyMove(boolean notifyMove) {
        saveDataManager.getRepository().getServerData(serverId).setNotifyMove(notifyMove);
    }

    @Override
    public int getReadLimit() {
        return saveDataManager.getRepository().getServerData(serverId).getReadLimit();
    }

    @Override
    public void setReadLimit(int readLimit) {
        saveDataManager.getRepository().getServerData(serverId).setReadLimit(readLimit);
    }

    @Override
    public int getNameReadLimit() {
        return saveDataManager.getRepository().getServerData(serverId).getNameReadLimit();
    }

    @Override
    public void setNameReadLimit(int nameReadLimit) {
        saveDataManager.getRepository().getServerData(serverId).setNameReadLimit(nameReadLimit);
    }

}
