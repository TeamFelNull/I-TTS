package dev.felnull.ttsvoice.core.tts;

import dev.felnull.ttsvoice.core.TTSVoiceRuntime;
import dev.felnull.ttsvoice.core.audio.LoadedSaidText;
import dev.felnull.ttsvoice.core.audio.VoiceAudioScheduler;
import dev.felnull.ttsvoice.core.tts.saidtext.SaidText;
import net.dv8tion.jda.api.entities.Guild;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class TTSInstance {
    private static final int MAX_COUNT = 150;
    private static final int LOAD_COUNT = 10;
    private final ConcurrentLinkedQueue<SaidText> saidTextQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<LoadedSaidTextEntry> loadSaidTextQueue = new ConcurrentLinkedQueue<>();
    private final AtomicReference<LoadedSaidTextEntry> currentSaidText = new AtomicReference<>();
    private final AtomicBoolean destroyed = new AtomicBoolean();
    private final Object updateLock = new Object();
    private final VoiceAudioScheduler voiceAudioScheduler;
    private final long audioChannel;
    private final long textChannel;
    private final boolean overwriteAloud;

    public TTSInstance(Guild guild, long audioChannel, long textChannel, boolean overwriteAloud) {
        this.voiceAudioScheduler = new VoiceAudioScheduler(guild.getAudioManager(), TTSVoiceRuntime.getInstance().getVoiceAudioManager());
        this.audioChannel = audioChannel;
        this.textChannel = textChannel;
        this.overwriteAloud = overwriteAloud;
    }

    public long getAudioChannel() {
        return audioChannel;
    }

    public long getTextChannel() {
        return textChannel;
    }

    public void dispose() {
        destroyed.set(true);

        saidTextQueue.clear();

        while (!loadSaidTextQueue.isEmpty())
            loadSaidTextQueue.poll().dispose();

        var cst = currentSaidText.get();
        if (cst != null)
            cst.dispose();

        voiceAudioScheduler.dispose();
    }

    public void sayText(SaidText saidText) {
        if (true) {
            TTSVoiceRuntime.getInstance().getCacheManager().test(saidText);
            return;
        }


        if (saidTextQueue.size() >= MAX_COUNT)
            return;

        if (overwriteAloud) {
            updateAloud(saidText);
        } else {
            saidTextQueue.add(saidText);
            updateQueue();
        }

        voiceAudioScheduler.test();
    }

    private void updateAloud(SaidText saidText) {
        synchronized (updateLock) {
            if (destroyed.get()) return;

            voiceAudioScheduler.stop();

            var cst = currentSaidText.getAndSet(new LoadedSaidTextEntry(saidText));
            if (cst != null)
                cst.dispose();

            sayStart();
        }
    }

    private void updateQueue() {
        synchronized (updateLock) {
            if (destroyed.get() || overwriteAloud) return;

            loadSaidTextQueue.removeIf(LoadedSaidTextEntry::isFailure);

            while (loadSaidTextQueue.size() < LOAD_COUNT && !saidTextQueue.isEmpty())
                loadSaidTextQueue.add(new LoadedSaidTextEntry(saidTextQueue.poll()));

            var cst = currentSaidText.get();
            if ((cst == null || cst.isFailure() || cst.isAlreadyUsed())) {
                if (cst != null)
                    cst.dispose();

                if (!loadSaidTextQueue.isEmpty()) {
                    currentSaidText.set(loadSaidTextQueue.poll());
                    sayStart();
                } else {
                    currentSaidText.set(null);
                }

                while (!saidTextQueue.isEmpty())
                    loadSaidTextQueue.add(new LoadedSaidTextEntry(saidTextQueue.poll()));
            }
        }
    }

    private void sayStart() {
        currentSaidText.get().completableFuture.whenCompleteAsync((loadedSaidText, throwable) -> {
            if (throwable != null) {
                if (!(throwable instanceof CancellationException))
                    TTSVoiceRuntime.getInstance().getLogger().error("Failed to load voice audio", throwable);
                if (!overwriteAloud) {
                    updateQueue();
                }

                return;
            }

            if (loadedSaidText.isFailure()) {
                updateQueue();
            } else {
                voiceAudioScheduler.play(loadedSaidText, () -> {
                    if (overwriteAloud) {
                        loadedSaidText.dispose();
                    } else {
                        updateQueue();
                    }
                });
            }
        }, TTSVoiceRuntime.getInstance().getAsyncWorkerExecutor());
    }

    private class LoadedSaidTextEntry {
        private final CompletableFuture<LoadedSaidText> completableFuture;
        private final Runnable stop;

        private LoadedSaidTextEntry(SaidText saidText) {
            var rl = voiceAudioScheduler.load(saidText);
            this.completableFuture = rl.getLeft();
            this.stop = rl.getRight();
        }

        private void dispose() {
            if (completableFuture.isDone()) {
                try {
                    completableFuture.get().dispose();
                } catch (InterruptedException | ExecutionException ignored) {
                }
            } else {
                completableFuture.cancel(false);
                stop.run();
            }

        }

        private boolean isFailure() {
            if (completableFuture.isDone()) {
                try {
                    return completableFuture.get().isFailure();
                } catch (InterruptedException | ExecutionException ignored) {
                }
            }
            return false;
        }

        private boolean isAlreadyUsed() {
            if (completableFuture.isDone()) {
                try {
                    return completableFuture.get().isAlreadyUsed();
                } catch (InterruptedException | ExecutionException ignored) {
                }
            }
            return false;
        }
    }
}
