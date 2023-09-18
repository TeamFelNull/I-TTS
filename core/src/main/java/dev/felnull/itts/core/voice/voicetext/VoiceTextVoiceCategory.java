package dev.felnull.itts.core.voice.voicetext;

import dev.felnull.itts.core.ITTSRuntime;
import dev.felnull.itts.core.voice.VoiceCategory;

/**
 * VoiceTextのカテゴリ
 *
 * @author MORIMORI0317
 */
public class VoiceTextVoiceCategory implements VoiceCategory {
    @Override
    public String getName() {
        return "VoiceText";
    }

    @Override
    public String getId() {
        return "voicetext";
    }

    @Override
    public boolean isAvailable() {
        return ITTSRuntime.getInstance().getVoiceManager().getVoiceTextManager().isAvailable();
    }
}
