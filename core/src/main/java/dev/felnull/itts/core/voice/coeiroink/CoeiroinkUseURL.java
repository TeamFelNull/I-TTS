package dev.felnull.itts.core.voice.coeiroink;

/**
 * CoeiroinkのURLを使用しているか確認するためのインターフェイス
 *
 * @author MORIMORI0317
 */
public interface CoeiroinkUseURL extends AutoCloseable {

    /**
     * CoeiroinkのURLを取得
     *
     * @return CoeiroinkエンジンのURL
     */
    CIURL getCIURL();
}
