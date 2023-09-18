package dev.felnull.itts.core.voice.voicevox;

/**
 * VOICEVOXのURLを使用しているか確認するためのインターフェイス
 *
 * @author MORIMORI0317
 */
public interface VoicevoxUseURL extends AutoCloseable {

    /**
     * VOICEVOXのURLを取得
     *
     * @return VOICEVOXエンジンのURL
     */
    VVURL getVVURL();
}
