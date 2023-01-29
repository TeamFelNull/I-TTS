package dev.felnull.ttsvoice.core.cache;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import dev.felnull.fnjl.util.FNDataUtil;
import dev.felnull.ttsvoice.core.TTSVoiceRuntime;
import dev.felnull.ttsvoice.core.tts.saidtext.SaidText;
import dev.felnull.ttsvoice.core.voice.voicetext.VTVoiceType;
import dev.felnull.ttsvoice.core.voice.voicetext.VoiceTextSpeaker;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class CacheManager {
    private static final File LOCAL_CACHE_FOLDER = new File("./tmp");
    private final Map<HashCode, CompletableFuture<LocalCache>> localCaches = new ConcurrentHashMap<>();
    private final Supplier<GlobalCacheAccess> globalCacheAccessFactory;

    public CacheManager(@Nullable Supplier<GlobalCacheAccess> globalCacheAccessFactory) {
        try {
            FileUtils.deleteDirectory(LOCAL_CACHE_FOLDER);
            FNDataUtil.wishMkdir(LOCAL_CACHE_FOLDER);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.globalCacheAccessFactory = globalCacheAccessFactory;
    }

    public CompletableFuture<CacheUseEntry> loadOrRestore(@NotNull HashCode key, @NotNull StreamOpener loadOpener) {
        return localCaches.computeIfAbsent(key, ky -> createLocalCache(ky, loadOpener)).thenApplyAsync(LocalCache::restore, getExecutor());
    }

    private CompletableFuture<LocalCache> createLocalCache(HashCode key, StreamOpener loadOpener) {
        CompletableFuture<File> cf;
        var lcFile = getLocalCacheFile(key);

        if (globalCacheAccessFactory != null) {
            cf = CompletableFuture.supplyAsync(() -> {
                try (var gca = globalCacheAccessFactory.get()) {
                    byte[] data = gca.get(key);

                    if (data == null) {
                        gca.lock(key);

                        data = gca.get(key);
                        if (data == null) {
                            try (var in = new BufferedInputStream(loadOpener.openStream());) {
                                data = in.readAllBytes();
                            }
                            gca.set(key, data);
                        }

                        gca.unlock(key);
                    }

                    Files.write(lcFile.toPath(), data);

                    return lcFile;
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }, getExecutor());

        } else {
            cf = CompletableFuture.supplyAsync(() -> {

                try (var in = loadOpener.openStream(); var out = new FileOutputStream(lcFile)) {
                    FNDataUtil.inputToOutputBuff(in, out);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }

                return lcFile;
            }, getExecutor());
        }
        return cf.thenApplyAsync((file) -> new LocalCache(key, file), getExecutor());
    }

    private File getLocalCacheFile(HashCode hashCode) {
        return new File(LOCAL_CACHE_FOLDER, hashCode.toString());
    }

    private Executor getExecutor() {
        return TTSVoiceRuntime.getInstance().getAsyncWorkerExecutor();
    }

    protected void disposeCache(HashCode hashCode) {
        var lc = localCaches.remove(hashCode);
        if (lc != null)
            lc.thenAcceptAsync(LocalCache::dispose, getExecutor());
    }

    public void test(SaidText saidText) {
        var hash = Hashing.murmur3_128().hashString(saidText.getText(), StandardCharsets.UTF_8);
        CacheUseEntry r;
        try {
            r = loadOrRestore(hash, () -> new VTVoiceType(VoiceTextSpeaker.SHOW).openVoiceStream(saidText.getText())).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        System.out.println(saidText.getText() + ": " + r.file());
        r.useLock().unlock();

        /*try (var glc = globalCacheAccessFactory.get()) {
            var hash = Hashing.murmur3_128().hashString("TEST", StandardCharsets.UTF_8);

            *//*System.out.println("ST");
            byte[] data;
            try (var st = new VTVoiceType(VoiceTextSpeaker.SHOW).openVoiceStream(saidText.getText())) {
                data = st.readAllBytes();
            }
            glc.set(hash, data);
            System.out.println("EN");*//*

            System.out.println(Arrays.toString(glc.get(hash)));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }*/
    }
}
