package dev.felnull.itts.core.statistics.repository;

import dev.felnull.itts.core.statistics.dao.StatisticsDAO;
import dev.felnull.itts.core.statistics.dao.StatisticsDAO.TTSCountSum;
import dev.felnull.itts.core.statistics.dao.StatisticsDAOFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TTSCountStatisticsTest {

    private static final long BOT_ID = 1031199180896620605L;
    private static final long SERVER_A = 600929948529590272L;
    private static final long SERVER_B = 436404936151007241L;
    private static final String VOICE_TYPE_A = "yajusenpai";
    private static final String VOICE_TYPE_B = "kbtit";
    private static final String VOICE_CATEGORY = "voicevox";

    @TempDir
    Path dbDir;

    private StatisticsDAO dao;
    private StatisticsRepository repo;

    @BeforeEach
    void setUp() {
        File dbFile = new File(dbDir.toFile(), "statistics_data.db");
        dao = StatisticsDAOFactory.getInstance().createSQLiteDAO(dbFile);
        repo = StatisticsRepository.create(dao);
        repo.init();
    }

    @AfterEach
    void tearDown() {
        repo.dispose();
    }

    @Test
    void testServerAndGlobalAccumulation() {
        LocalDate today = LocalDate.now();
        repo.increment(BOT_ID, SERVER_A, VOICE_TYPE_A, VOICE_CATEGORY, today, 100L, 1L);
        repo.increment(BOT_ID, SERVER_A, VOICE_TYPE_A, VOICE_CATEGORY, today, 50L, 1L);
        repo.increment(BOT_ID, SERVER_B, VOICE_TYPE_B, VOICE_CATEGORY, today, 30L, 1L);

        TTSCountSum serverA = repo.sumCount(BOT_ID, SERVER_A, today, today);
        assertEquals(150L, serverA.charCount());
        assertEquals(2L, serverA.messageCount());

        TTSCountSum serverB = repo.sumCount(BOT_ID, SERVER_B, today, today);
        assertEquals(30L, serverB.charCount());
        assertEquals(1L, serverB.messageCount());

        TTSCountSum global = repo.sumCount(BOT_ID, null, today, today);
        assertEquals(180L, global.charCount());
        assertEquals(3L, global.messageCount());
    }

    @Test
    void testRangeAndAllSums() {
        LocalDate d1 = LocalDate.of(2026, 1, 1);
        LocalDate d2 = LocalDate.of(2026, 1, 2);
        LocalDate d3 = LocalDate.of(2026, 1, 3);

        repo.increment(BOT_ID, SERVER_A, VOICE_TYPE_A, VOICE_CATEGORY, d1, 10L, 1L);
        repo.increment(BOT_ID, SERVER_A, VOICE_TYPE_A, VOICE_CATEGORY, d2, 20L, 1L);
        repo.increment(BOT_ID, SERVER_A, VOICE_TYPE_B, VOICE_CATEGORY, d3, 40L, 1L);
        repo.increment(BOT_ID, SERVER_B, VOICE_TYPE_A, VOICE_CATEGORY, d2, 5L, 1L);

        TTSCountSum range = repo.sumCount(BOT_ID, SERVER_A, d1, d2);
        assertEquals(30L, range.charCount());
        assertEquals(2L, range.messageCount());

        TTSCountSum all = repo.sumCount(BOT_ID, null, null, null);
        assertEquals(75L, all.charCount());
        assertEquals(4L, all.messageCount());

        TTSCountSum serverAll = repo.sumCount(BOT_ID, SERVER_A, null, null);
        assertEquals(70L, serverAll.charCount());
        assertEquals(3L, serverAll.messageCount());
    }

    @Test
    void testConcurrentIncrementNoLostWrites() throws InterruptedException {
        LocalDate today = LocalDate.now();
        int threadCount = 8;
        int perThread = 50;

        ExecutorService es = Executors.newFixedThreadPool(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        AtomicInteger failures = new AtomicInteger();

        for (int t = 0; t < threadCount; t++) {
            final int threadIndex = t;
            es.submit(() -> {
                try {
                    start.await();
                    String voiceType = (threadIndex % 2 == 0) ? VOICE_TYPE_A : VOICE_TYPE_B;
                    for (int i = 0; i < perThread; i++) {
                        repo.increment(BOT_ID, SERVER_A, voiceType, VOICE_CATEGORY, today, 2L, 1L);
                    }
                } catch (Exception e) {
                    failures.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        assertTrue(done.await(30, TimeUnit.SECONDS));
        es.shutdown();

        assertEquals(0, failures.get());

        TTSCountSum sum = repo.sumCount(BOT_ID, SERVER_A, today, today);
        long expectedChars = (long) threadCount * perThread * 2L;
        long expectedMessages = (long) threadCount * perThread;
        assertEquals(expectedChars, sum.charCount());
        assertEquals(expectedMessages, sum.messageCount());
    }

    @Test
    void testNullVoiceFallsBackToUnknownKey() {
        LocalDate today = LocalDate.now();
        repo.increment(BOT_ID, SERVER_A, null, null, today, 11L, 1L);
        repo.increment(BOT_ID, SERVER_A, null, null, today, 22L, 1L);

        TTSCountSum sum = repo.sumCount(BOT_ID, SERVER_A, today, today);
        assertEquals(33L, sum.charCount());
        assertEquals(2L, sum.messageCount());
    }
}
