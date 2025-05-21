package dev.felnull.itts.core.voice.coeiroink;

import dev.felnull.itts.core.voice.VoiceCategory;

import java.util.Locale;

/**
 * Coeiroinkのカテゴリ
 *
 * @author MORIMORI0317
 */
public class CoeiroinkVoiceCategory implements VoiceCategory {

    /**
     * Coeiroinkのマネージャー
     */
    private final CoeiroinkManager manager;

    /**
     * コンストラクタ
     *
     * @param coeiroinkManager マネージャー
     */
    public CoeiroinkVoiceCategory(CoeiroinkManager coeiroinkManager) {
        this.manager = coeiroinkManager;
    }

    @Override
    public String getName() {
        return manager.getName().toUpperCase(Locale.ROOT);
    }

    @Override
    public String getId() {
        return manager.getName().toLowerCase(Locale.ROOT);
    }

    @Override
    public boolean isAvailable() {
        return manager.isAvailable();
    }
}
