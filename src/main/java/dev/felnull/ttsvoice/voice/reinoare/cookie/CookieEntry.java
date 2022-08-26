package dev.felnull.ttsvoice.voice.reinoare.cookie;

import dev.felnull.ttsvoice.voice.reinoare.ReinoareEntry;

import java.util.UUID;

public record CookieEntry(String name, String path, UUID uuid) implements ReinoareEntry {
    @Override
    public String getURL() {
        return CookieManager.getInstance().getFileURL(uuid);
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
