package dev.felnull.itts.core;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

/**
 * 二重起動防止用ディレクトリロック
 *
 * @author MORIMORI0317
 */
public class DirectoryLock implements ITTSRuntimeUse {

    /**
     * ロック用ファイル
     */
    private static final File LOCK_FILE = new File("./dir.lock");

    /**
     * ロックを実行
     */
    protected void lock() {
        FileChannel channel = null;
        try {
            // 同じファイルへのロックを維持するため、起動時も終了時も削除しない
            channel = FileChannel.open(LOCK_FILE.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            if (channel.tryLock() == null) {
                throw new IOException("Directory is already locked");
            }
        } catch (IOException | RuntimeException e) {
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException closeException) {
                    e.addSuppressed(closeException);
                }
            }
            throw new RuntimeException("Failed to lock directory, directory may be locked by another process", e);
        }

        FileChannel lockedChannel = channel;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                lockedChannel.close();
            } catch (IOException ignored) {
                // ファイルロックの解放に失敗した場合は諦める
            }
        }));
    }
}
