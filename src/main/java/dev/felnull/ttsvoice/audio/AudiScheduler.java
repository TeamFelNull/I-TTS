package dev.felnull.ttsvoice.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import dev.felnull.fnjl.util.FNMath;
import dev.felnull.ttsvoice.Main;
import dev.felnull.ttsvoice.audio.loader.VoiceLoaderManager;
import dev.felnull.ttsvoice.audio.player.VoiceTrackLoader;
import dev.felnull.ttsvoice.tts.BotAndGuild;
import dev.felnull.ttsvoice.tts.TTSManager;
import dev.felnull.ttsvoice.tts.TTSVoiceEntry;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AudiScheduler extends AudioEventAdapter {
    private static final int previsionLoadCount = Runtime.getRuntime().availableProcessors();
    private static final ExecutorService executorService = Executors.newFixedThreadPool(previsionLoadCount, new BasicThreadFactory.Builder().namingPattern("voice-tack-loader-%d").daemon(true).build());
    private final Map<TTSVoiceEntry, CompletableFuture<AudioTrack>> previsionLoadTracks = new HashMap<>();
    private final AudioPlayer player;
    private final BotAndGuild botAndGuild;
    private final Object nextLock = new Object();
    private final Object stopLock = new Object();
    private boolean loading;
    private Thread loadThread;
    private CoolDownThread coolDownThread;
    private VoiceTrackLoader currentTrackLoader;

    public AudiScheduler(AudioPlayer player, BotAndGuild bag) {
        this.player = player;
        this.player.addListener(this);
        var guild = bag.getGuild();
        guild.getAudioManager().setSendingHandler(new AudioPlayerSendHandler(player));
        this.botAndGuild = bag;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        currentTrackLoader.afterEnd();
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
            var queue = tm.getTTSQueue(botAndGuild);
            TTSVoiceEntry next;
            synchronized (queue) {
                next = queue.poll();
            }
            if (next == null) return false;
            loading = true;
            loadThread = Thread.currentThread();
            var loader = vlm.getTrackLoader(next.voice());
            if (loader == null) {
                startCoolDown();
                loading = false;
                loadThread = null;
                return true;
            }

            AudioTrack track;
            try {
                CompletableFuture<AudioTrack> loaded;
                synchronized (previsionLoadTracks) {
                    loaded = previsionLoadTracks.remove(next);
                }

                if (loaded == null)
                    loaded = loader.loaded();

                track = loaded.get();
                currentTrackLoader = loader;

                if (!Main.getServerConfig(botAndGuild.guildId()).isOverwriteAloud()) {
                    synchronized (queue) {
                        int lc = FNMath.clamp(queue.size(), 0, previsionLoadCount);
                        if (lc >= 1) {
                            for (int i = 0; i < lc; i++) {
                                var l = queue.get(queue.size() - 1 - i);
                                var ll = CompletableFuture.supplyAsync(() -> vlm.getTrackLoader(l.voice()), executorService).thenApplyAsync(n -> {
                                    try {
                                        return n.loaded().get();
                                    } catch (InterruptedException | ExecutionException e) {
                                        throw new RuntimeException(e);
                                    }
                                }, executorService);
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
