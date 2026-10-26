package dev.felnull.itts.core.watchdog;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Map;

/**
 * ThreadNameClassifierのテストクラス
 */
public class ThreadNameClassifierTest {

    @ParameterizedTest
    @DisplayName("スレッド名が分類される")
    @CsvSource(delimiter = '|', value = {
            "JDA AudioWS-ReadThread (guildId: 1558798316307882024)|jda-audio-read",
            "JDA AudioWS-WriteThread (guildId: 1)|jda-audio-write",
            "JDA AudioWS-ConnectThread (guildId: 1)|jda-audio-connect",
            "JDA AudioConnection Guild: 1365262829187502131 Sending Thread|jda-audio-send",
            "JDA MainWS-ReadThread|jda-other",
            "lava-daemon-pool-playback-1-thread-1493|lava-playback",
            "lava-daemon-pool-info-loader-2-thread-3|lava-other",
            "async-worker-2537|async",
            "http-worker-3|http",
            "heavy-worker-thread-1|heavy",
            "I-TTS Pool:housekeeper|hikari",
            "I-TTS Statistics Pool:connection-adder|hikari",
            "Thread-20496|unnamed",
            "immortality-timer|immortality-timer",
            "receive-thread-12|receive-thread-N"
    })
    void classifies(String name, String expected) {
        Assertions.assertEquals(expected, ThreadNameClassifier.classify(name));
    }

    @Test
    @DisplayName("名前がなければ unnamed")
    void nullAndEmpty() {
        Assertions.assertEquals("unnamed", ThreadNameClassifier.classify(null));
        Assertions.assertEquals("unnamed", ThreadNameClassifier.classify(""));
    }

    @Test
    @DisplayName("本数の多い順に集計される")
    void countsSortedByDescending() {
        Map<String, Integer> counts = ThreadNameClassifier.count(List.of(
                "async-worker-1", "async-worker-2", "async-worker-3",
                "http-worker-1",
                "JDA AudioWS-ReadThread (guildId: 1)", "JDA AudioWS-ReadThread (guildId: 2)"));
        Assertions.assertEquals(List.of("async", "jda-audio-read", "http"), List.copyOf(counts.keySet()));
        Assertions.assertEquals(3, counts.get("async"));
        Assertions.assertEquals(2, counts.get("jda-audio-read"));
    }

    @Test
    @DisplayName("固定表示の分類は本数が0でも表示される")
    void pinnedAlwaysShown() {
        Map<String, Integer> counts = ThreadNameClassifier.count(List.of("a-1", "a-2"));
        Assertions.assertEquals("audio=0 a-N=2", ThreadNameClassifier.format(counts, List.of("audio"), 5));
    }

    @Test
    @DisplayName("固定表示の分類は本数が少なくても others に埋もれない")
    void pinnedNotHiddenInOthers() {
        List<String> names = new java.util.ArrayList<>();
        for (int i = 0; i < 5; i++) {
            names.add("worker-" + i + "-x");
            names.add("other" + i + "-a");
            names.add("other" + i + "-a");
        }
        names.add("JDA AudioWS-ReadThread (guildId: 1)");
        String line = ThreadNameClassifier.format(ThreadNameClassifier.count(names), List.of("jda-audio-read"), 1);
        Assertions.assertTrue(line.startsWith("jda-audio-read=1"), line);
    }

    @Test
    @DisplayName("固定表示以外は上限まで表示し、残りは others にまとめられる")
    void othersAreSummarized() {
        Map<String, Integer> counts = ThreadNameClassifier.count(List.of("a-1", "a-2", "b-1", "c-1", "d-1"));
        Assertions.assertEquals("a-N=2 b-N=1 others=2", ThreadNameClassifier.format(counts, List.of(), 2));
        Assertions.assertEquals("", ThreadNameClassifier.format(Map.of(), List.of(), 5));
    }
}
