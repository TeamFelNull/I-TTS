package dev.felnull.ttsvoice.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import dev.felnull.fnjl.util.FNDataUtil;
import dev.felnull.fnjl.util.FNMath;
import dev.felnull.ttsvoice.Main;
import dev.felnull.ttsvoice.audio.loader.VoiceLoaderManager;
import dev.felnull.ttsvoice.audio.player.VoiceTrackLoader;
import dev.felnull.ttsvoice.discord.BotLocation;
import dev.felnull.ttsvoice.tts.TTSManager;
import dev.felnull.ttsvoice.tts.TTSVoiceEntry;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class AudioScheduler extends AudioEventAdapter {
    private static final Function<BotLocation, ExecutorService> EXECUTOR_SERVICES = FNDataUtil.memoize(bag -> Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new BasicThreadFactory.Builder().namingPattern("voice-tack-loader-" + bag.guildId() + "-" + bag.botUserId() + "-%d").daemon(true).build()));
    private final int previsionLoadCount = 10;
    private final Map<TTSVoiceEntry, CompletableFuture<Pair<VoiceTrackLoader, AudioTrack>>> previsionLoadTracks = new HashMap<>();
    private final Map<TTSVoiceEntry, VoiceTrackLoader> loaders = new HashMap<>();
    private final AudioPlayer player;
    private final BotLocation botLocation;
    private final Object nextLock = new Object();
    private final Object stopLock = new Object();
    private boolean loading;
    private Thread loadThread;
    private CoolDownThread coolDownThread;
    private VoiceTrackLoader currentTrackLoader;
    protected boolean destroy;

    public AudioScheduler(AudioPlayer player, BotLocation bag) {
        this.player = player;
        this.player.addListener(this);
        var guild = bag.getGuild();
        guild.getAudioManager().setSendingHandler(new AudioPlayerSendHandler(player));
        this.botLocation = bag;
    }

    private ExecutorService getExecutorService() {
        synchronized (EXECUTOR_SERVICES) {
            return EXECUTOR_SERVICES.apply(botLocation);
        }
    }

    public void dispose() {
        this.destroy = true;

        if (coolDownThread != null)
            coolDownThread.interrupt();

        if (loading && loadThread != null)
            loadThread.interrupt();

        if (currentTrackLoader != null)
            currentTrackLoader.end();

        synchronized (loaders) {
            for (VoiceTrackLoader value : loaders.values()) {
                value.end();
            }
        }

        player.destroy();

        var guild = botLocation.getGuild();
        guild.getAudioManager().setSendingHandler(null);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (currentTrackLoader != null)
            currentTrackLoader.end();
        currentTrackLoader = null;
        startCoolDown();
    }

    private void startCoolDown() {
        if (coolDownThread != null) {
            coolDownThread.interrupt();
        }
        coolDownThread = new CoolDownThread();
        coolDownThread.start();
    }

    public void play(AudioTrack track, float volume) {
        player.startTrack(track, false);
        player.setVolume((int) (100 * volume));
    }

    public boolean isLoadingOrPlaying() {
        return (coolDownThread != null && coolDownThread.isAlive()) || player.getPlayingTrack() != null || loading;
    }

    public void stop() {
        synchronized (stopLock) {
            if (loadThread != null) {
                loadThread.interrupt();
                loadThread = null;
                loading = false;
            }
            player.stopTrack();
            if (coolDownThread != null) {
                coolDownThread.interrupt();
                coolDownThread = null;
            }
        }
    }

    public boolean next() {
        synchronized (nextLock) {
            var vlm = VoiceLoaderManager.getInstance();
            var tm = TTSManager.getInstance();
            var queue = tm.getTTSQueue(botLocation);
            TTSVoiceEntry next;
            synchronized (queue) {
                next = queue.poll();
            }
            if (next == null) return false;
            loading = true;
            loadThread = Thread.currentThread();

            AudioTrack track;
            try {
                CompletableFuture<Pair<VoiceTrackLoader, AudioTrack>> loaded;
                synchronized (previsionLoadTracks) {
                    loaded = previsionLoadTracks.remove(next);
                }
                synchronized (loaders) {
                    loaders.remove(next);
                }

                if (loaded == null) {
                    var l = vlm.getTrackLoader(next.voice());
                    if (l != null) {
                        l.setAudioScheduler(this);
                        loaded = l.loaded().thenApply(n -> Pair.of(l, n));
                    }
                }

                if (loaded == null) {
                    startCoolDown();
                    loading = false;
                    loadThread = null;
                    return true;
                }

                var lg = loaded.get();
                track = lg.getRight();

                if (currentTrackLoader != null)
                    currentTrackLoader.end();

                currentTrackLoader = lg.getLeft();

                if (!Main.getServerSaveData(botLocation.guildId()).isOverwriteAloud()) {
                    synchronized (queue) {
                        List<TTSVoiceEntry> qc = queue.stream().filter(n -> !previsionLoadTracks.containsKey(n)).toList();
                        int lc = FNMath.clamp(qc.size(), 0, previsionLoadCount);
                        if (lc >= 1) {
                            for (int i = 0; i < lc; i++) {
                                var l = qc.get(i);
                                var ll = CompletableFuture.supplyAsync(() -> {
                                    var tl = vlm.getTrackLoader(l.voice());
                                    if (tl != null)
                                        tl.setAudioScheduler(this);
                                    return tl;
                                }, getExecutorService()).thenApplyAsync(n -> {
                                    try {
                                        synchronized (loaders) {
                                            loaders.put(l, n);
                                        }
                                        return Pair.of(n, n.loaded().get());
                                    } catch (InterruptedException | ExecutionException e) {
                                        throw new RuntimeException(e);
                                    }
                                }, getExecutorService());
                                synchronized (previsionLoadTracks) {
                                    previsionLoadTracks.put(l, ll);
                                }
                            }
                        }
                    }
                }

            } catch (Exception ex) {
                startCoolDown();
                loading = false;
                loadThread = null;
                return true;
            }
            play(track, next.voice().voiceType().getVolume());
            loading = false;
            loadThread = null;
            return true;
        }
    }

    public boolean isDestroy() {
        return destroy;
    }

    private class CoolDownThread extends Thread {
        @Override
        public void run() {
            try {
                Thread.sleep(1000);
                next();
            } catch (InterruptedException ignored) {
            }
        }
    }
}
