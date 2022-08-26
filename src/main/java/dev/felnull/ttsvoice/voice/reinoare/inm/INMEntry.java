package dev.felnull.ttsvoice.voice.reinoare.inm;

import dev.felnull.ttsvoice.voice.reinoare.ReinoareEntry;

import java.util.UUID;

public record INMEntry(String name, String path, UUID uuid) implements ReinoareEntry {
    @Override
    public String getURL() {
        return INMManager.getInstance().getFileURL(uuid);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }
}
