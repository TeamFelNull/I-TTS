package dev.felnull.itts.core.watchdog;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.OptionalLong;

/**
 * WatchdogPolicyのテストクラス
 */
public class WatchdogPolicyTest {

    private static final WatchdogPolicy.ConnectionState OK = WatchdogPolicy.ConnectionState.HEALTHY;

    private static final WatchdogPolicy.ConnectionState DOWN = WatchdogPolicy.ConnectionState.DISCONNECTED;

    private static WatchdogPolicy policy() {
        return new WatchdogPolicy(Duration.ofMinutes(5), 0.95);
    }

    @Nested
    @DisplayName("接続状態の判定")
    class ConnectionTests {

        @Test
        @DisplayName("接続できている間は正常")
        void healthyIsOk() {
            Assertions.assertFalse(policy().evaluateConnection(0, OK).fatal());
        }

        @Test
        @DisplayName("切断が許容時間に満たなければ正常")
        void shortDisconnectIsOk() {
            WatchdogPolicy p = policy();
            Assertions.assertFalse(p.evaluateConnection(0, DOWN).fatal());
            Assertions.assertFalse(p.evaluateConnection(299_999, DOWN).fatal());
        }

        @Test
        @DisplayName("切断が許容時間に達したら終了")
        void longDisconnectIsFatal() {
            WatchdogPolicy p = policy();
            p.evaluateConnection(1_000, DOWN);
            WatchdogPolicy.Verdict v = p.evaluateConnection(301_000, DOWN);
            Assertions.assertTrue(v.fatal());
            Assertions.assertFalse(v.reason().isEmpty());
        }

        @Test
        @DisplayName("途中で復旧したら時間は数え直す")
        void recoveryResetsTimer() {
            WatchdogPolicy p = policy();
            p.evaluateConnection(0, DOWN);
            p.evaluateConnection(200_000, DOWN);
            p.evaluateConnection(210_000, OK);
            // 復旧後の再切断は、新しく数え始める
            Assertions.assertFalse(p.evaluateConnection(220_000, DOWN).fatal());
            Assertions.assertFalse(p.evaluateConnection(500_000, DOWN).fatal());
            Assertions.assertTrue(p.evaluateConnection(520_000, DOWN).fatal());
        }
    }

    @Nested
    @DisplayName("スレッド数の判定")
    class ThreadTests {

        @Test
        @DisplayName("上限が不明なら判定しない")
        void unknownLimitIsOk() {
            Assertions.assertFalse(policy().evaluateThreads(OptionalLong.of(10_000), OptionalLong.empty()).fatal());
            Assertions.assertFalse(policy().evaluateThreads(OptionalLong.empty(), OptionalLong.of(512)).fatal());
            Assertions.assertFalse(policy().evaluateThreads(OptionalLong.of(10), OptionalLong.of(0)).fatal());
        }

        @Test
        @DisplayName("使用率が閾値未満なら正常")
        void belowThresholdIsOk() {
            Assertions.assertFalse(policy().evaluateThreads(OptionalLong.of(486), OptionalLong.of(512)).fatal());
        }

        @Test
        @DisplayName("使用率が閾値以上なら終了")
        void atThresholdIsFatal() {
            Assertions.assertTrue(policy().evaluateThreads(OptionalLong.of(487), OptionalLong.of(512)).fatal());
            Assertions.assertTrue(policy().evaluateThreads(OptionalLong.of(512), OptionalLong.of(512)).fatal());
        }
    }

    @Nested
    @DisplayName("引数の検証")
    class ConstructorTests {

        @Test
        @DisplayName("不正な値は拒否する")
        void rejectsInvalid() {
            Assertions.assertThrows(IllegalArgumentException.class, () -> new WatchdogPolicy(Duration.ZERO, 0.9));
            Assertions.assertThrows(IllegalArgumentException.class, () -> new WatchdogPolicy(Duration.ofSeconds(-1), 0.9));
            Assertions.assertThrows(IllegalArgumentException.class, () -> new WatchdogPolicy(Duration.ofMinutes(1), 0));
            Assertions.assertThrows(IllegalArgumentException.class, () -> new WatchdogPolicy(Duration.ofMinutes(1), 1.1));
        }
    }
}
