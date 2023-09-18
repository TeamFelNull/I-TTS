package dev.felnull.itts;

import dev.felnull.itts.core.ITTSRuntime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * セルフホスト版I-TTSのMain
 *
 * @author MORIMORI0317
 */
public class Main {
    /**
     * ロガー
     */
    public static final Logger LOGGER = LogManager.getLogger(Main.class);

    /**
     * ランタイム
     */
    public static ITTSRuntime runtime;

    private Main() {
    }

    /**
     * main関数
     *
     * @param args プログラム引数
     */
    public static void main(String[] args) {
        runtime = ITTSRuntime.newRuntime(new SelfHostITTSRuntimeContext());
        runtime.execute();
    }
}