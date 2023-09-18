package dev.felnull.itts.core.voice;

import dev.felnull.itts.core.ITTSBaseManager;
import dev.felnull.itts.core.savedata.SaveDataManager;
import dev.felnull.itts.core.savedata.ServerUserData;
import dev.felnull.itts.core.voice.voicetext.VoiceTextManager;
import dev.felnull.itts.core.voice.voicevox.VoicevoxManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 声関係の管理を行うクラス
 *
 * @author MORIMORI0317
 */
public class VoiceManager implements ITTSBaseManager {

    /**
     * VoiceTextの管理
     */
    private final VoiceTextManager voiceTextManager = new VoiceTextManager();

    /**
     * VOICEVOXの管理
     */
    private final VoicevoxManager voicevoxManager =
            new VoicevoxManager("voicevox", () ->
                    getConfigManager().getConfig().getVoicevoxConfig().getApiUrls(), () -> getConfigManager().getConfig().getVoicevoxConfig());

    /**
     * COEIROINKの管理
     */
    private final VoicevoxManager coeiroinkManager =
            new VoicevoxManager("coeiroink", () ->
                    getConfigManager().getConfig().getCoeirolnkConfig().getApiUrls(), () -> getConfigManager().getConfig().getCoeirolnkConfig());

    /**
     * SHAREVOXの管理
     */
    private final VoicevoxManager sharevoxManager =
            new VoicevoxManager("sharevox", () ->
                    getConfigManager().getConfig().getSharevoxConfig().getApiUrls(), () -> getConfigManager().getConfig().getSharevoxConfig());

    /**
     * 全音声タイプ
     */
    private final List<Supplier<List<VoiceType>>> voiceTypes = new ArrayList<>();

    private void registerVoiceTypes(Supplier<List<VoiceType>> availableVoiceTypes) {
        voiceTypes.add(availableVoiceTypes);
    }

    @Override
    public @NotNull CompletableFuture<?> init() {
        return CompletableFuture.allOf(
                        voicevoxManager.init(),
                        coeiroinkManager.init(),
                        sharevoxManager.init()).
                thenAcceptAsync(v -> {
                    registerVoiceTypes(voiceTextManager::getVoiceTypes);
                    registerVoiceTypes(voicevoxManager::getAvailableVoiceTypes);
                    registerVoiceTypes(coeiroinkManager::getAvailableVoiceTypes);
                    registerVoiceTypes(sharevoxManager::getAvailableVoiceTypes);

                    getITTSLogger().info("Voice initial setup complete");
                }, getAsyncExecutor());
    }

    public VoiceTextManager getVoiceTextManager() {
        return voiceTextManager;
    }

    public VoicevoxManager getVoicevoxManager() {
        return voicevoxManager;
    }

    public VoicevoxManager getCoeiroinkManager() {
        return coeiroinkManager;
    }

    public VoicevoxManager getSharevoxManager() {
        return sharevoxManager;
    }

    @NotNull
    @Unmodifiable
    public Map<VoiceCategory, List<VoiceType>> getAvailableVoiceTypes() {
        return voiceTypes.stream()
                .flatMap(n -> n.get().stream())
                .filter(VoiceType::isAvailable)
                .collect(Collectors.groupingBy(VoiceType::getCategory));
    }

    /**
     * IDから声カテゴリを取得
     *
     * @param id 声ID
     * @return 声カテゴリ
     */
    public Optional<VoiceCategory> getVoiceCategory(String id) {
        return getAvailableVoiceTypes().keySet().stream()
                .filter(r -> r.getId().equals(id))
                .findAny();
    }

    /**
     * IDから声タイプを取得
     *
     * @param id 音声タイプID
     * @return 声タイプ
     */
    public Optional<VoiceType> getVoiceType(String id) {
        return getAvailableVoiceTypes().values().stream()
                .flatMap(Collection::stream)
                .filter(v -> v.getId().equals(id))
                .findAny();
    }

    @Nullable
    public VoiceType getDefaultVoiceType() {
        return getAvailableVoiceTypes().values().stream()
                .flatMap(Collection::stream)
                .min(Comparator.comparingInt(vt -> voiceTextManager.getCategory() == vt.getCategory() ? 1 : 0))
                .orElse(null);
    }

    /**
     * デフォルトの声タイプを取得
     *
     * @param guildId サーバーID
     * @return 声タイプ
     */
    @Nullable
    public VoiceType getDefaultVoiceType(long guildId) {
        String defaultVt = getSaveDataManager().getServerData(guildId).getDefaultVoiceType();

        if (defaultVt == null) {
            return getDefaultVoiceType();
        }

        return getVoiceType(defaultVt).orElseGet(this::getDefaultVoiceType);
    }

    /**
     * 声タイプを取得
     *
     * @param guildId サーバーID
     * @param userId  ユーザーID
     * @return 声タイプ
     */
    @Nullable
    public VoiceType getVoiceType(long guildId, long userId) {
        SaveDataManager sdm = getSaveDataManager();
        ServerUserData serverUserData = sdm.getServerUserData(guildId, userId);
        Optional<VoiceType> vt = getVoiceType(serverUserData.getVoiceType());

        return vt.orElseGet(() -> getDefaultVoiceType(guildId));
    }
}
