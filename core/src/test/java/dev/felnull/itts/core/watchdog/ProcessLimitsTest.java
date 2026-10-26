package dev.felnull.itts.core.watchdog;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.OptionalLong;

/**
 * ProcessLimitsのテストクラス
 */
public class ProcessLimitsTest {

    @Test
    @DisplayName("上限の数値が読める")
    void parsesLimit() {
        Assertions.assertEquals(OptionalLong.of(512), ProcessLimits.parseLimit("512\n"));
    }

    @Test
    @DisplayName("maxは無制限として空を返す")
    void maxIsUnlimited() {
        Assertions.assertTrue(ProcessLimits.parseLimit("max\n").isEmpty());
    }

    @Test
    @DisplayName("不正な値は空を返す")
    void invalidIsEmpty() {
        Assertions.assertTrue(ProcessLimits.parseLimit(null).isEmpty());
        Assertions.assertTrue(ProcessLimits.parseLimit("").isEmpty());
        Assertions.assertTrue(ProcessLimits.parseLimit("abc").isEmpty());
        Assertions.assertTrue(ProcessLimits.parseNumber("-1").isEmpty());
    }

    @Test
    @DisplayName("ステータスからスレッド数を取り出せる")
    void parsesThreadsLine() {
        String status = "Name:\tjava\nThreads:\t61\nVmRSS:\t1000 kB\n";
        Assertions.assertEquals(OptionalLong.of(61), ProcessLimits.parseThreadsLine(status));
        Assertions.assertTrue(ProcessLimits.parseThreadsLine("Name:\tjava\n").isEmpty());
        Assertions.assertTrue(ProcessLimits.parseThreadsLine(null).isEmpty());
    }

    @Test
    @DisplayName("実環境で読んでも例外にならない")
    void readNeverThrows() {
        Assertions.assertNotNull(ProcessLimits.read());
    }
}
