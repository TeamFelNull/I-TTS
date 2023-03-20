package dev.felnull.itts.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * 二重起動防止用ディレクトリロック
 */
public class DirectoryLock implements ITTSRuntimeUse {
    private static final File LOCK_FILE = new File("./dir.lock");
    private FileOutputStream fileOutputStream;
    private FileChannel fileChannel;
    private FileLock fileLock;

    protected void lock() {
        LOCK_FILE.delete();

        try {
            fileOutputStream = new FileOutputStream(LOCK_FILE);
            fileOutputStream.write(new byte[]{0});
            fileChannel = fileOutputStream.getChannel();
            fileLock = fileChannel.tryLock();
        } catch (IOException e) {
            throw new RuntimeException("Failed to lock directory, directory may be locked by another process", e);
        }


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                fileLock.release();
            } catch (Exception ignored) {
            }
            try {
                fileOutputStream.close();
            } catch (Exception ignored) {
            }
            LOCK_FILE.delete();
        }));
    }
}
