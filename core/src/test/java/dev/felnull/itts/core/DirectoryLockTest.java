package dev.felnull.itts.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 別プロセス間のディレクトリロックを検証する
 */
class DirectoryLockTest {
    @TempDir
    Path directory;

    @Test
    @Timeout(30)
    void rejectsConcurrentProcessesAndAllowsRestart() throws Exception {
        Process first = start();
        try {
            assertEquals("LOCKED", first.inputReader().readLine());
            Process second = start();
            try {
                assertTrue(second.waitFor(10, TimeUnit.SECONDS));
                assertNotEquals(0, second.exitValue());
                assertTrue(Files.exists(directory.resolve("dir.lock")));
                Process third = start();
                try {
                    assertTrue(third.waitFor(10, TimeUnit.SECONDS));
                    assertNotEquals(0, third.exitValue());
                } finally {
                    third.destroyForcibly();
                }
            } finally {
                second.destroyForcibly();
            }
            first.getOutputStream().close();
            assertTrue(first.waitFor(10, TimeUnit.SECONDS));
            assertEquals(0, first.exitValue());
            assertTrue(Files.exists(directory.resolve("dir.lock")));
            Process restarted = start();
            try {
                assertEquals("LOCKED", restarted.inputReader().readLine());
                restarted.getOutputStream().close();
                assertTrue(restarted.waitFor(10, TimeUnit.SECONDS));
                assertEquals(0, restarted.exitValue());
            } finally {
                restarted.destroyForcibly();
            }
        } finally {
            first.destroyForcibly();
        }
    }

    private Process start() throws Exception {
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        return new ProcessBuilder(java, "-cp", System.getProperty("java.class.path"), LockProcess.class.getName())
                .directory(directory.toFile())
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start();
    }

    /**
     * 標準入力が閉じられるまでロックを保持する子プロセス
     */
    public static class LockProcess {
        /**
         * ロックを取得して終了指示を待つ
         *
         * @param args 起動引数
         * @throws Exception ロックの取得や標準入力の読み込みに失敗した場合
         */
        public static void main(String[] args) throws Exception {
            new DirectoryLock().lock();
            System.out.println("LOCKED");
            System.in.read();
        }
    }
}
