package dev.felnull.ttsvoice.core.voice;

import dev.felnull.ttsvoice.core.voice.voicetext.VoiceTextManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class VoiceManager {
    private final VoiceTextManager voiceTextManager = new VoiceTextManager();
    private final List<Supplier<List<VoiceType>>> voiceTypes = new ArrayList<>();

    private void registerVoiceTypes(Supplier<List<VoiceType>> availableVoiceTypes) {
        voiceTypes.add(availableVoiceTypes);
    }

    public void init() {
        registerVoiceTypes(voiceTextManager::getVoiceTypes);
    }

    public VoiceTextManager getVoiceTextManager() {
        return voiceTextManager;
    }

    @NotNull
    @Unmodifiable
    public Map<VoiceCategory, List<VoiceType>> getAvailableVoiceTypes() {
        return voiceTypes.stream()
                .flatMap(n -> n.get().stream())
                .filter(VoiceType::isAvailable)
                .collect(Collectors.groupingBy(VoiceType::getCategory));
    }
}
