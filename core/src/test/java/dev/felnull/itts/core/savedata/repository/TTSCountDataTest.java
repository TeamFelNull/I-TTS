package dev.felnull.itts.core.savedata.repository;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TTSCountDataTest extends RepoBaseTest {

    @Test
    void testServerAndGlobalAccumulation() {
        DataRepository repo = createRepository();
        long botId = 1234567890123L;
        long serverId = 9876543210987L;
        LocalDate date = LocalDate.now(ZoneOffset.UTC);

        TTSCountData server = repo.getServerTTSCount(botId, serverId, date);
        TTSCountData global = repo.getGlobalTTSCount(botId, date);

        assertEquals(0L, server.getCharCount());
        assertEquals(0L, server.getMessageCount());
        assertEquals(0L, global.getCharCount());
        assertEquals(0L, global.getMessageCount());

        server.addCount(10L, 1L);
        server.addCount(5L, 1L);
        global.addCount(15L, 2L);

        assertEquals(15L, server.getCharCount());
        assertEquals(2L, server.getMessageCount());
        assertEquals(15L, global.getCharCount());
        assertEquals(2L, global.getMessageCount());

        repo.dispose();
    }

    @Test
    void testRangeAndAllSums() {
        DataRepository repo = createRepository();
        long botId = 11111L;
        long serverId = 22222L;
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate yesterday = today.minusDays(1);

        repo.getServerTTSCount(botId, serverId, today).addCount(20L, 2L);
        repo.getServerTTSCount(botId, serverId, yesterday).addCount(7L, 1L);
        repo.getGlobalTTSCount(botId, today).addCount(20L, 2L);
        repo.getGlobalTTSCount(botId, yesterday).addCount(7L, 1L);

        assertEquals(27L, repo.sumServerCharCount(botId, serverId, yesterday, today));
        assertEquals(20L, repo.sumServerCharCount(botId, serverId, today, today));
        assertEquals(27L, repo.sumGlobalCharCount(botId, yesterday, today));
        assertEquals(27L, repo.sumServerAllCharCount(botId, serverId));
        assertEquals(3L, repo.sumServerAllMessageCount(botId, serverId));
        assertEquals(27L, repo.sumGlobalAllCharCount(botId));
        assertEquals(3L, repo.sumGlobalAllMessageCount(botId));

        repo.dispose();
    }

    @Test
    void testConcurrentIncrementNoLostWrites() throws InterruptedException {
        DataRepository repo = createRepository();
        long botId = 555L;
        long serverId = 666L;
        LocalDate date = LocalDate.now(ZoneOffset.UTC);

        int threadCount = 8;
        int incrementsPerThread = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        AtomicInteger errors = new AtomicInteger();
        List<Throwable> errorList = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    start.await();
                    TTSCountData server = repo.getServerTTSCount(botId, serverId, date);
                    TTSCountData global = repo.getGlobalTTSCount(botId, date);
                    for (int j = 0; j < incrementsPerThread; j++) {
                        server.addCount(1L, 1L);
                        global.addCount(1L, 1L);
                    }
                } catch (Throwable t) {
                    errors.incrementAndGet();
                    synchronized (errorList) {
                        errorList.add(t);
                    }
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        assertTrue(done.await(60, TimeUnit.SECONDS), "concurrent increment did not finish in time");
        executor.shutdown();

        assertEquals(0, errors.get(), () -> "errors during concurrent increment: " + errorList);

        long expected = (long) threadCount * incrementsPerThread;
        TTSCountData server = repo.getServerTTSCount(botId, serverId, date);
        TTSCountData global = repo.getGlobalTTSCount(botId, date);
        assertEquals(expected, server.getCharCount(), "server char count lost writes");
        assertEquals(expected, server.getMessageCount(), "server message count lost writes");
        assertEquals(expected, global.getCharCount(), "global char count lost writes");
        assertEquals(expected, global.getMessageCount(), "global message count lost writes");

        repo.dispose();
    }
}
