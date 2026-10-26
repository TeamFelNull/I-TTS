package dev.felnull.itts.core.watchdog;

import net.dv8tion.jda.api.JDA;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Watchdogのテストクラス
 */
public class WatchdogTest {

    private static final long LIMIT_MS = Duration.ofMinutes(5).toMillis();

    private final AtomicLong now = new AtomicLong(0);

    private final List<Integer> halts = new ArrayList<>();

    private final AtomicReference<JDA.Status> status = new AtomicReference<>(JDA.Status.CONNECTED);

    private final AtomicReference<ProcessLimits.Snapshot> limits = new AtomicReference<>(
            new ProcessLimits.Snapshot(OptionalLong.of(512), OptionalLong.of(100), "test"));

    private Logger logger;

    private Watchdog watchdog;

    @BeforeEach
    void setUp() {
        this.logger = Mockito.mock(Logger.class);
        JDA jda = Mockito.mock(JDA.class);
        Mockito.when(jda.getStatus()).thenAnswer(invocation -> this.status.get());

        this.watchdog = new Watchdog(this.logger, new WatchdogPolicy(Duration.ofMinutes(5), 0.95),
                () -> jda, () -> 3, this.now::get, this.halts::add,
                () -> List.of("async-worker-1", "main"), this.limits::get, duration -> {
        });
    }

    @Nested
    @DisplayName("JDAの状態の分類")
    class ClassifyTests {

        @Test
        @DisplayName("切断中の状態は切断として扱う")
        void disconnectedStates() {
            for (JDA.Status s : List.of(JDA.Status.DISCONNECTED, JDA.Status.RECONNECT_QUEUED,
                    JDA.Status.WAITING_TO_RECONNECT, JDA.Status.ATTEMPTING_TO_RECONNECT)) {
                Assertions.assertEquals(WatchdogPolicy.ConnectionState.DISCONNECTED, Watchdog.classify(s), s.name());
            }
        }

        @Test
        @DisplayName("接続済み・起動中・終了処理中は対象外")
        void healthyStates() {
            for (JDA.Status s : List.of(JDA.Status.CONNECTED, JDA.Status.INITIALIZING, JDA.Status.LOGGING_IN,
                    JDA.Status.LOADING_SUBSYSTEMS, JDA.Status.SHUTTING_DOWN, JDA.Status.SHUTDOWN)) {
                Assertions.assertEquals(WatchdogPolicy.ConnectionState.HEALTHY, Watchdog.classify(s), s.name());
            }
        }
    }

    @Nested
    @DisplayName("tick()の動作")
    class TickTests {

        @Test
        @DisplayName("正常なら何も終了しない")
        void healthy() {
            WatchdogTest.this.watchdog.tick();
            Assertions.assertTrue(WatchdogTest.this.halts.isEmpty());
        }

        @Test
        @DisplayName("切断が長く続くと強制終了し、理由をERRORで出す")
        void disconnectedTooLong() {
            WatchdogTest.this.status.set(JDA.Status.WAITING_TO_RECONNECT);
            WatchdogTest.this.watchdog.tick();
            Assertions.assertTrue(WatchdogTest.this.halts.isEmpty());

            WatchdogTest.this.now.set(LIMIT_MS);
            WatchdogTest.this.watchdog.tick();
            Assertions.assertEquals(1, WatchdogTest.this.halts.size());
            Mockito.verify(WatchdogTest.this.logger).error(Mockito.contains("terminating"));
        }

        @Test
        @DisplayName("切断中に復旧すれば終了しない")
        void recovers() {
            WatchdogTest.this.status.set(JDA.Status.ATTEMPTING_TO_RECONNECT);
            WatchdogTest.this.watchdog.tick();
            WatchdogTest.this.now.set(LIMIT_MS - 1);
            WatchdogTest.this.status.set(JDA.Status.CONNECTED);
            WatchdogTest.this.watchdog.tick();
            WatchdogTest.this.now.set(LIMIT_MS * 3);
            WatchdogTest.this.watchdog.tick();
            Assertions.assertTrue(WatchdogTest.this.halts.isEmpty());
        }

        @Test
        @DisplayName("定期ログには音声スレッドが0本でも表示され、totalの出典が付く")
        void reportShowsPinnedCategoriesAndSource() {
            WatchdogTest.this.watchdog.start();
            WatchdogTest.this.watchdog.stop();
            Mockito.clearInvocations(WatchdogTest.this.logger);

            WatchdogTest.this.now.set(Duration.ofMinutes(10).toMillis());
            WatchdogTest.this.watchdog.tick();

            ArgumentCaptor<Object> args = ArgumentCaptor.forClass(Object.class);
            Mockito.verify(WatchdogTest.this.logger).info(Mockito.startsWith("Threads:"), args.capture(), args.capture(), args.capture(),
                    args.capture(), args.capture(), args.capture(), args.capture());
            List<Object> values = args.getAllValues();
            Assertions.assertEquals("100", values.get(0));
            Assertions.assertEquals("test", values.get(1));
            String breakdown = String.valueOf(values.get(4));
            Assertions.assertTrue(breakdown.contains("jda-audio-read=0"), breakdown);
            Assertions.assertTrue(breakdown.contains("async=1"), breakdown);
        }

        @Test
        @DisplayName("OSから現在値を読めない時は、totalの出典がjvmになる")
        void reportSourceFallsBackToJvm() {
            WatchdogTest.this.limits.set(new ProcessLimits.Snapshot(OptionalLong.empty(), OptionalLong.empty(), "unavailable"));
            WatchdogTest.this.watchdog.start();
            WatchdogTest.this.watchdog.stop();
            Mockito.clearInvocations(WatchdogTest.this.logger);

            WatchdogTest.this.now.set(Duration.ofMinutes(10).toMillis());
            WatchdogTest.this.watchdog.tick();

            ArgumentCaptor<Object> args = ArgumentCaptor.forClass(Object.class);
            Mockito.verify(WatchdogTest.this.logger).info(Mockito.startsWith("Threads:"), args.capture(), args.capture(), args.capture(),
                    args.capture(), args.capture(), args.capture(), args.capture());
            Assertions.assertEquals("jvm", args.getAllValues().get(1));
        }

        @Test
        @DisplayName("スレッド数が上限に近づくと強制終了する")
        void threadsNearLimit() {
            WatchdogTest.this.limits.set(new ProcessLimits.Snapshot(OptionalLong.of(512), OptionalLong.of(490), "test"));
            WatchdogTest.this.watchdog.tick();
            Assertions.assertEquals(1, WatchdogTest.this.halts.size());
        }

        @Test
        @DisplayName("上限が不明ならスレッド数では終了しない")
        void unknownLimit() {
            WatchdogTest.this.limits.set(new ProcessLimits.Snapshot(OptionalLong.empty(), OptionalLong.of(100_000), "test"));
            WatchdogTest.this.watchdog.tick();
            Assertions.assertTrue(WatchdogTest.this.halts.isEmpty());
        }

        @Test
        @DisplayName("JDAの起動前は正常として扱う")
        void jdaNotStarted() {
            Watchdog w = new Watchdog(WatchdogTest.this.logger, new WatchdogPolicy(Duration.ofMinutes(5), 0.95),
                    () -> null, () -> 0, WatchdogTest.this.now::get, WatchdogTest.this.halts::add,
                    List::of, WatchdogTest.this.limits::get, duration -> {
            });
            WatchdogTest.this.now.set(LIMIT_MS * 10);
            w.tick();
            Assertions.assertTrue(WatchdogTest.this.halts.isEmpty());
        }

        @Test
        @DisplayName("10分おきに状態を1行でINFOに出す")
        void periodicReport() {
            WatchdogTest.this.watchdog.start();
            WatchdogTest.this.watchdog.stop();
            Mockito.clearInvocations(WatchdogTest.this.logger);

            WatchdogTest.this.now.set(Duration.ofMinutes(10).toMillis());
            WatchdogTest.this.watchdog.tick();
            Mockito.verify(WatchdogTest.this.logger).info(Mockito.startsWith("Threads:"), Mockito.any(), Mockito.any(), Mockito.any(),
                    Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

            Mockito.clearInvocations(WatchdogTest.this.logger);
            WatchdogTest.this.now.set(Duration.ofMinutes(11).toMillis());
            WatchdogTest.this.watchdog.tick();
            Mockito.verify(WatchdogTest.this.logger, Mockito.never()).info(Mockito.startsWith("Threads:"), Mockito.any(), Mockito.any(),
                    Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        }
    }

    @Test
    @DisplayName("監視スレッドは1本だけ作られ、停止できる")
    void startsOneDaemonThreadAndStops() throws InterruptedException {
        long before = Thread.getAllStackTraces().keySet().stream().filter(t -> Watchdog.THREAD_NAME.equals(t.getName())).count();

        this.watchdog.start();
        this.watchdog.start();
        long during = Thread.getAllStackTraces().keySet().stream().filter(t -> Watchdog.THREAD_NAME.equals(t.getName())).count();
        Assertions.assertEquals(before + 1, during);

        Thread thread = Thread.getAllStackTraces().keySet().stream()
                .filter(t -> Watchdog.THREAD_NAME.equals(t.getName())).findFirst().orElseThrow();
        Assertions.assertTrue(thread.isDaemon());

        this.watchdog.stop();
        thread.join(2000);
        Assertions.assertFalse(thread.isAlive());
    }

    @Test
    @DisplayName("判定中に例外が起きても監視は続き、終了もしない")
    void survivesExceptions() throws InterruptedException {
        AtomicLong calls = new AtomicLong();
        Watchdog w = new Watchdog(this.logger, new WatchdogPolicy(Duration.ofMinutes(5), 0.95),
                () -> {
                    if (calls.incrementAndGet() <= 2) {
                        throw new IllegalStateException("boom");
                    }
                    return null;
                }, () -> 0, this.now::get, this.halts::add, List::of, this.limits::get, duration -> Thread.sleep(5));

        w.start();
        long deadline = System.currentTimeMillis() + 3000;
        while (calls.get() < 4 && System.currentTimeMillis() < deadline) {
            Thread.sleep(10);
        }
        w.stop();

        Assertions.assertTrue(calls.get() >= 4, "例外のあとも監視が続くこと: " + calls.get());
        Assertions.assertTrue(this.halts.isEmpty());
        Mockito.verify(this.logger, Mockito.atLeastOnce()).error(Mockito.contains("Watchdog check failed"));
    }
}
