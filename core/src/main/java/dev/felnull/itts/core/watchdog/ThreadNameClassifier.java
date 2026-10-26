package dev.felnull.itts.core.watchdog;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * スレッド名を種類ごとに分類して集計する
 * 数字やサーバーIDなどの可変部分は無視して、同じ役割のスレッドを同じ分類にまとめる
 */
public final class ThreadNameClassifier {

    /**
     * 数字の連続
     */
    private static final Pattern DIGITS = Pattern.compile("\\d+");

    /**
     * 名前のないスレッド
     */
    private static final Pattern UNNAMED = Pattern.compile("^Thread-\\d+$");

    private ThreadNameClassifier() {
    }

    /**
     * スレッド名を分類名に変換する
     *
     * @param name スレッド名
     * @return 分類名
     */
    public static String classify(String name) {
        if (name == null || name.isEmpty()) {
            return "unnamed";
        }

        if (name.startsWith("JDA AudioWS-ReadThread")) {
            return "jda-audio-read";
        }
        if (name.startsWith("JDA AudioWS-WriteThread")) {
            return "jda-audio-write";
        }
        if (name.startsWith("JDA AudioWS-ConnectThread")) {
            return "jda-audio-connect";
        }
        if (name.startsWith("JDA AudioConnection") && name.endsWith("Sending Thread")) {
            return "jda-audio-send";
        }
        if (name.startsWith("JDA ")) {
            return "jda-other";
        }
        if (name.startsWith("lava-daemon-pool-playback")) {
            return "lava-playback";
        }
        if (name.startsWith("lava-daemon-pool")) {
            return "lava-other";
        }
        if (name.startsWith("async-worker")) {
            return "async";
        }
        if (name.startsWith("http-worker")) {
            return "http";
        }
        if (name.startsWith("heavy-worker")) {
            return "heavy";
        }
        if (name.startsWith("HttpClient")) {
            return "httpclient";
        }
        if (name.contains("Pool:") || name.startsWith("HikariPool")) {
            return "hikari";
        }
        if (UNNAMED.matcher(name).matches()) {
            return "unnamed";
        }

        return DIGITS.matcher(name).replaceAll("N");
    }

    /**
     * スレッド名の一覧を分類ごとに集計する
     * 本数の多い分類が先頭になる
     *
     * @param names スレッド名
     * @return 分類名と本数
     */
    public static Map<String, Integer> count(Collection<String> names) {
        Map<String, Integer> raw = new LinkedHashMap<>();
        for (String name : names) {
            raw.merge(classify(name), 1, Integer::sum);
        }

        Map<String, Integer> sorted = new LinkedHashMap<>();
        raw.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .forEach(e -> sorted.put(e.getKey(), e.getValue()));
        return sorted;
    }

    /**
     * 集計結果を1行の文字列にする
     *
     * <p>常に表示したい分類は、本数が少なくても必ず表示する。
     * 残りは本数の多いものから上限まで表示し、それ以外は others にまとめる。</p>
     *
     * @param counts    分類ごとの本数
     * @param pinned    常に表示する分類名。本数が0でも表示する
     * @param maxOthers 常に表示する分類以外から表示する分類数の上限
     * @return 1行の文字列
     */
    public static String format(Map<String, Integer> counts, Collection<String> pinned, int maxOthers) {
        StringBuilder sb = new StringBuilder();

        for (String key : pinned) {
            appendItem(sb, key, counts.getOrDefault(key, 0));
        }

        int shown = 0;
        int rest = 0;
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            if (pinned.contains(e.getKey())) {
                continue;
            }

            if (shown < maxOthers) {
                appendItem(sb, e.getKey(), e.getValue());
                shown++;
            } else {
                rest += e.getValue();
            }
        }

        if (rest > 0) {
            appendItem(sb, "others", rest);
        }

        return sb.toString();
    }

    private static void appendItem(StringBuilder sb, String key, int value) {
        if (sb.length() > 0) {
            sb.append(' ');
        }
        sb.append(key).append('=').append(value);
    }
}
