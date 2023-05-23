package dev.felnull.itts.core.voice.voicevox;

import dev.felnull.itts.core.voice.Voice;
import dev.felnull.itts.core.voice.VoiceCategory;
import dev.felnull.itts.core.voice.VoiceType;

public class VoicevoxVoiceType implements VoiceType {
    private final VoicevoxSpeaker voicevoxSpeaker;
    private final VoicevoxManager manager;

    public VoicevoxVoiceType(VoicevoxSpeaker voicevoxSpeaker, VoicevoxManager voicevoxManager) {
        this.voicevoxSpeaker = voicevoxSpeaker;
        this.manager = voicevoxManager;
    }

    @Override
    public String getName() {
        return this.voicevoxSpeaker.name();
    }

    @Override
    public String getId() {
        return manager.getName() + "-" + this.voicevoxSpeaker.uuid().toString();
    }

    @Override
    public boolean isAvailable() {
        return manager.isAvailable() && manager.getBalancer().getAvailableSpeakers().contains(voicevoxSpeaker);
    }

    @Override
    public VoiceCategory getCategory() {
        return manager.getCategory();
    }

    @Override
    public Voice createVoice(long guildId, long userId) {
        return new VoicevoxVoice(this, manager, voicevoxSpeaker);
    }
}
