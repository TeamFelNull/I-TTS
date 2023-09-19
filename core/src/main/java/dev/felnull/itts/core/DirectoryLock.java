package dev.felnull.itts.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

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
     * ロック用アウトプットストリーム
     */
    private FileOutputStream fileOutputStream;

    /**
     * ファイルロック
     */
    private FileLock fileLock;

    private String TEST_CODE;

    /**
     * ロックを実行
     */
    protected void lock() {
        if (LOCK_FILE.exists() && !LOCK_FILE.delete()) {
            throw new RuntimeException("Failed to delete old lock file");
        }

        try {
            fileOutputStream = new FileOutputStream(LOCK_FILE);
            fileOutputStream.write(new byte[]{0});
            FileChannel fileChannel = fileOutputStream.getChannel();
            fileLock = fileChannel.tryLock();
        } catch (IOException e) {
            throw new RuntimeException("Failed to lock directory, directory may be locked by another process", e);
        }


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                fileLock.release();
            } catch (Exception ignored) {
                // ファイルロックの解放に失敗した場合は諦める
            }
            try {
                fileOutputStream.close();
            } catch (Exception ignored) {
                // ファイルロック用アウトプットストリームを閉じることに失敗した場合も諦める
            }

            if (LOCK_FILE.exists() && !LOCK_FILE.delete()) {
                throw new RuntimeException("Failed to delete lock file");
            }
        }));
    }
}
