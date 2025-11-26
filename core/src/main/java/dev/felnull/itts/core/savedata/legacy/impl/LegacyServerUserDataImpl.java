package dev.felnull.itts.core.savedata.legacy.impl;

import dev.felnull.itts.core.savedata.SaveDataManager;
import dev.felnull.itts.core.savedata.legacy.LegacyServerUserData;
import org.jetbrains.annotations.Nullable;

/**
 * レガシーサーバー別ユーザーデータの実装
 */
final class LegacyServerUserDataImpl implements LegacyServerUserData {

    /**
     * セーブデータマネージャー
     */
    private final SaveDataManager saveDataManager;

    /**
     * サーバーID
     */
    private final long serverId;

    /**
     * ユーザーID
     */
    private final long userId;

    /**
     * コンストラクタ
     *
     * @param saveDataManager セーブデータマネージャー
     * @param serverId        サーバーID
     * @param userId          ユーザーID
     */
    LegacyServerUserDataImpl(SaveDataManager saveDataManager, long serverId, long userId) {
        this.saveDataManager = saveDataManager;
        this.serverId = serverId;
        this.userId = userId;
    }

    @Override
    public @Nullable String getVoiceType() {
        return saveDataManager.getRepository().getServerUserData(serverId, userId).getVoiceType();
    }

    @Override
    public void setVoiceType(@Nullable String voiceType) {
        saveDataManager.getRepository().getServerUserData(serverId, userId).setVoiceType(voiceType);
    }

    @Override
    public boolean isDeny() {
        return saveDataManager.getRepository().getServerUserData(serverId, userId).isDeny();
    }

    @Override
    public void setDeny(boolean deny) {
        saveDataManager.getRepository().getServerUserData(serverId, userId).setDeny(deny);
    }

    @Override
    public @Nullable String getNickName() {
        return saveDataManager.getRepository().getServerUserData(serverId, userId).getNickName();
    }

    @Override
    public void setNickName(@Nullable String nickName) {
        saveDataManager.getRepository().getServerUserData(serverId, userId).setNickName(nickName);
    }
}
