package dev.felnull.itts.core.voice;

import dev.felnull.itts.core.TTSVoiceRuntime;
import dev.felnull.itts.core.config.ConfigManager;
import dev.felnull.itts.core.voice.voicetext.VoiceTextManager;
import dev.felnull.itts.core.voice.voicevox.VoicevoxManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class VoiceManager {
    private final VoiceTextManager voiceTextManager = new VoiceTextManager();
    private final VoicevoxManager voicevoxManager = new VoicevoxManager("voicevox", () -> getConfigManager().getConfig().getVoicevoxConfig().getApiUrls(), () -> getConfigManager().getConfig().getVoicevoxConfig());
    private final VoicevoxManager coeiroinkManager = new VoicevoxManager("coeiroink", () -> getConfigManager().getConfig().getCoeirolnkConfig().getApiUrls(), () -> getConfigManager().getConfig().getCoeirolnkConfig());
    private final VoicevoxManager sharevoxManager = new VoicevoxManager("sharevox", () -> getConfigManager().getConfig().getSharevoxConfig().getApiUrls(), () -> getConfigManager().getConfig().getSharevoxConfig());

    private final List<Supplier<List<VoiceType>>> voiceTypes = new ArrayList<>();

    private void registerVoiceTypes(Supplier<List<VoiceType>> availableVoiceTypes) {
        voiceTypes.add(availableVoiceTypes);
    }

    private ConfigManager getConfigManager() {
        return TTSVoiceRuntime.getInstance().getConfigManager();
    }

    public void init() {
        voicevoxManager.init();
        coeiroinkManager.init();
        sharevoxManager.init();

        registerVoiceTypes(voiceTextManager::getVoiceTypes);
        registerVoiceTypes(voicevoxManager::getAvailableVoiceTypes);
        registerVoiceTypes(coeiroinkManager::getAvailableVoiceTypes);
        registerVoiceTypes(sharevoxManager::getAvailableVoiceTypes);
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
                .findFirst().orElse(null);
    }

    @Nullable
    public VoiceType getDefaultVoiceType(long guildId) {
        var defaultVt = TTSVoiceRuntime.getInstance().getSaveDataManager().getServerData(guildId).getDefaultVoiceType();

        if (defaultVt == null)
            return getDefaultVoiceType();

        return getVoiceType(defaultVt).orElseGet(this::getDefaultVoiceType);
    }

    @Nullable
    public VoiceType getVoiceType(long guildId, long userId) {
        var sdm = TTSVoiceRuntime.getInstance().getSaveDataManager();
        var serverUserData = sdm.getServerUserData(guildId, userId);
        var vt = getVoiceType(serverUserData.getVoiceType());

        return vt.orElseGet(() -> getDefaultVoiceType(guildId));
    }
}
