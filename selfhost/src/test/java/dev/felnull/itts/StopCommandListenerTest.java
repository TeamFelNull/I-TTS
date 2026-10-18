package dev.felnull.itts;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 標準入力の停止コマンド受付を検証する
 */
class StopCommandListenerTest {
    private static int stopCount(String input) {
        AtomicInteger count = new AtomicInteger();
        StopCommandListener.listen(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), count::incrementAndGet);
        return count.get();
    }

    @Test
    void stopsOnStopLineAfterOtherInput() {
        assertEquals(1, stopCount("help\r\n  STOP \r\nstop\n"));
    }

    @Test
    void doesNotStopWithoutStopLine() {
        assertEquals(0, stopCount("stopping\nrestart\n"));
    }

    @Test
    void doesNotStopOnClosedInput() {
        assertEquals(0, stopCount(""));
    }
}
