package dev.felnull.itts.core.watchdog;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.OptionalLong;

/**
 * プロセスが作成できるスレッド数(PID数)の上限と現在値を読み取る
 * コンテナのcgroupを優先して読み、読めない場合は取得不可として扱う
 */
public final class ProcessLimits {

    /**
     * cgroup v2の上限ファイル
     */
    private static final Path CGROUP_V2_MAX = Path.of("/sys/fs/cgroup/pids.max");

    /**
     * cgroup v2の現在値ファイル
     */
    private static final Path CGROUP_V2_CURRENT = Path.of("/sys/fs/cgroup/pids.current");

    /**
     * cgroup v1の上限ファイル
     */
    private static final Path CGROUP_V1_MAX = Path.of("/sys/fs/cgroup/pids/pids.max");

    /**
     * cgroup v1の現在値ファイル
     */
    private static final Path CGROUP_V1_CURRENT = Path.of("/sys/fs/cgroup/pids/pids.current");

    /**
     * プロセス情報のステータスファイル
     */
    private static final Path PROC_STATUS = Path.of("/proc/self/status");

    private ProcessLimits() {
    }

    /**
     * 上限と現在値を読み取る
     *
     * @return 上限と現在値
     */
    public static Snapshot read() {
        Snapshot v2 = readFrom(CGROUP_V2_MAX, CGROUP_V2_CURRENT, "cgroup-v2");
        if (v2.limit().isPresent() || v2.current().isPresent()) {
            return v2;
        }

        Snapshot v1 = readFrom(CGROUP_V1_MAX, CGROUP_V1_CURRENT, "cgroup-v1");
        if (v1.limit().isPresent() || v1.current().isPresent()) {
            return v1;
        }

        OptionalLong threads = readProcStatusThreads();
        return new Snapshot(OptionalLong.empty(), threads, threads.isPresent() ? "proc-status" : "unavailable");
    }

    private static Snapshot readFrom(Path maxFile, Path currentFile, String source) {
        return new Snapshot(parseLimit(readFile(maxFile)), parseNumber(readFile(currentFile)), source);
    }

    /**
     * 上限ファイルの内容を解釈する
     * `max`は無制限として扱い、空を返す
     *
     * @param text ファイルの内容
     * @return 上限
     */
    static OptionalLong parseLimit(@Nullable String text) {
        if (text == null) {
            return OptionalLong.empty();
        }

        String trimmed = text.strip();
        if ("max".equals(trimmed)) {
            return OptionalLong.empty();
        }

        return parseNumber(trimmed);
    }

    /**
     * 数値だけが書かれたファイルの内容を解釈する
     *
     * @param text ファイルの内容
     * @return 数値
     */
    static OptionalLong parseNumber(@Nullable String text) {
        if (text == null) {
            return OptionalLong.empty();
        }

        try {
            long value = Long.parseLong(text.strip());
            return value < 0 ? OptionalLong.empty() : OptionalLong.of(value);
        } catch (NumberFormatException e) {
            return OptionalLong.empty();
        }
    }

    /**
     * ステータスファイルの内容から現在のスレッド数を取り出す
     *
     * @param text ファイルの内容
     * @return スレッド数
     */
    static OptionalLong parseThreadsLine(@Nullable String text) {
        if (text == null) {
            return OptionalLong.empty();
        }

        for (String line : text.split("\\R")) {
            if (line.startsWith("Threads:")) {
                return parseNumber(line.substring("Threads:".length()));
            }
        }

        return OptionalLong.empty();
    }

    private static OptionalLong readProcStatusThreads() {
        return parseThreadsLine(readFile(PROC_STATUS));
    }

    @Nullable
    private static String readFile(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    /**
     * 上限と現在値
     *
     * @param limit   上限。取得できない場合や無制限の場合は空
     * @param current 現在値。取得できない場合は空
     * @param source  取得元の説明
     */
    public record Snapshot(OptionalLong limit, OptionalLong current, String source) {
    }
}
