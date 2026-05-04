package dev.felnull.itts;

import dev.felnull.itts.core.ITTSRuntime;
import dev.felnull.itts.core.RuntimeInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

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
        String implV = Main.class.getPackage().getImplementationVersion();
        RuntimeInfo info = new RuntimeInfo(implV == null, Objects.requireNonNullElse(implV, "None"));

        runtime = ITTSRuntime.newRuntime(new SelfHostITTSRuntimeContext(info));
        runtime.execute();
    }
}