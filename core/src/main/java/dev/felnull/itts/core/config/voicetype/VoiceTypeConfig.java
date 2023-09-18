package dev.felnull.itts.core.config.voicetype;

/**
 * 読み上げ音声の共通コンフィグ
 *
 * @author MORIMORI0317
 */
public interface VoiceTypeConfig {
    /**
     * デフォルトは有効かどうか
     */
    boolean DEFAULT_ENABLE = true;

    /**
     * 有効かどうか取得
     *
     * @return 有効かどうか
     */
    boolean isEnable();
}
