package dev.felnull.itts.core.voice;

import dev.felnull.itts.core.ITTSBaseManager;
import dev.felnull.itts.core.voice.voicetext.VoiceTextManager;
import dev.felnull.itts.core.voice.voicevox.VoicevoxManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class VoiceManager implements ITTSBaseManager {
    private final VoiceTextManager voiceTextManager = new VoiceTextManager();
    private final VoicevoxManager voicevoxManager = new VoicevoxManager("voicevox", () -> getConfigManager().getConfig().getVoicevoxConfig().getApiUrls(), () -> getConfigManager().getConfig().getVoicevoxConfig());
    private final VoicevoxManager coeiroinkManager = new VoicevoxManager("coeiroink", () -> getConfigManager().getConfig().getCoeirolnkConfig().getApiUrls(), () -> getConfigManager().getConfig().getCoeirolnkConfig());
    private final VoicevoxManager sharevoxManager = new VoicevoxManager("sharevox", () -> getConfigManager().getConfig().getSharevoxConfig().getApiUrls(), () -> getConfigManager().getConfig().getSharevoxConfig());

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

    public Optional<VoiceCategory> getVoiceCategory(String id) {
        return getAvailableVoiceTypes().keySet().stream()
                .filter(r -> r.getId().equals(id))
                .findAny();
    }

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

    @Nullable
    public VoiceType getDefaultVoiceType(long guildId) {
        var defaultVt = getSaveDataManager().getServerData(guildId).getDefaultVoiceType();

        if (defaultVt == null)
            return getDefaultVoiceType();

        return getVoiceType(defaultVt).orElseGet(this::getDefaultVoiceType);
    }

    @Nullable
    public VoiceType getVoiceType(long guildId, long userId) {
        var sdm = getSaveDataManager();
        var serverUserData = sdm.getServerUserData(guildId, userId);
        var vt = getVoiceType(serverUserData.getVoiceType());

        return vt.orElseGet(() -> getDefaultVoiceType(guildId));
    }
}
