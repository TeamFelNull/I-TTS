package dev.felnull.ttsvoice.inm;

import java.util.UUID;

public record INMEntry(String name, String path, UUID uuid) {
    public String getURL() {
        return INMManager.getInstance().getFileURL(uuid);
    }
}
