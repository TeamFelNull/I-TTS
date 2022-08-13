package dev.felnull.ttsvoice.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import dev.felnull.ttsvoice.discord.BotLocation;
import dev.felnull.ttsvoice.tts.TTSVoiceEntry;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.commons.lang3.tuple.Pair;

import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AudioScheduler extends AudioEventAdapter {
    private final ExecutorService playExecutorService;
    protected final LinkedList<TTSVoiceEntry> ttsQueue = new LinkedList<>();
    private final AudioLoader audioLoader;
    protected final BotLocation botLocation;
    private final AudioPlayer audioPlayer;
    private final Object lock = new Object();
    private volatile UUID runtimeId = UUID.randomUUID();
    private CoolDownThread coolDownThread;
    private Pair<TTSVoiceEntry, AudioEntry> currentEntry;

    public AudioScheduler(BotLocation botLocation, AudioPlayer audioPlayer) {
        this.botLocation = botLocation;
        this.audioPlayer = audioPlayer;
        this.audioPlayer.addListener(this);
        botLocation.getGuild().getAudioManager().setSendingHandler(new AudioPlayerSendHandler(audioPlayer));
        this.playExecutorService = Executors.newSingleThreadExecutor(new BasicThreadFactory.Builder().namingPattern("voice-tack-player-" + botLocation.guildId() + "-" + botLocation.botUserId()).daemon(true).build());
        this.audioLoader = new AudioLoader(this);
    }

    public void reload() {
        synchronized (lock) {
            stop();
            synchronized (ttsQueue) {
                this.ttsQueue.clear();
            }

            this.audioLoader.reload();
            this.runtimeId = UUID.randomUUID();

            end();

            if (coolDownThread != null) {
                coolDownThread.interrupt();
                coolDownThread = null;
            }
        }
    }

    public UUID getRuntimeId() {
        return runtimeId;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        synchronized (lock) {
            end();
            if (coolDownThread == null) {
                coolDownThread = new CoolDownThread(this::next);
                coolDownThread.start();
            }
        }
    }

    private void end() {
        if (currentEntry != null) {
            currentEntry.getLeft().trackerDepose();
            currentEntry.getRight().trackLoader().end();
            currentEntry = null;
        }
    }

    public void addEntry(TTSVoiceEntry entry) {
        synchronized (lock) {
            synchronized (ttsQueue) {
                this.ttsQueue.add(entry);
            }

            audioLoader.addLoadEntry(entry, ae -> {
                next(Pair.of(entry, ae));
            }, playExecutorService);
        }
    }

    public void next(Pair<TTSVoiceEntry, AudioEntry> entry) {
        synchronized (lock) {
            TTSVoiceEntry lst;
            synchronized (ttsQueue) {
                lst = ttsQueue.getFirst();
            }
            if (audioPlayer.getPlayingTrack() == null && coolDownThread == null) {
                boolean flg;
                synchronized (audioLoader.loadingEntry) {
                    var cf = audioLoader.loadingEntry.get(lst);
                    flg = cf == null || cf.isDone();
                }
                if (flg) {
                    if (entry != null && lst.equals(entry.getKey())) {
                        synchronized (ttsQueue) {
                            ttsQueue.removeFirst();
                        }
                        synchronized (audioLoader.loadingEntry) {
                            audioLoader.loadingEntry.remove(entry.getKey());
                        }
                        play(entry.getKey(), entry.getValue());
                    } else {
                        next();
                    }
                }
            }
        }
    }

    private void next() {
        synchronized (ttsQueue) {
            synchronized (audioLoader.loadingEntry) {
                if (ttsQueue.isEmpty()) return;
                var lst = ttsQueue.getFirst();
                if (lst != null) {
                    var cf = audioLoader.loadingEntry.get(lst);
                    if (cf != null && cf.isDone()) {
                        cf.thenAcceptAsync(ae -> play(lst, ae), playExecutorService);
                        audioLoader.loadingEntry.remove(lst);
                    }
                    ttsQueue.removeFirst();
                }
            }
        }
    }

    public void start(TTSVoiceEntry entry) {
        audioLoader.stopAllLoad();
        audioLoader.addLoadEntry(entry, ae -> play(entry, ae), playExecutorService);
    }

    private synchronized void play(TTSVoiceEntry voiceEntry, AudioEntry audioEntry) {
        synchronized (lock) {
            end();
            currentEntry = Pair.of(voiceEntry, audioEntry);

            audioPlayer.startTrack(audioEntry.audioTrack(), false);
            audioPlayer.setVolume((int) (100 * voiceEntry.voice().voiceType().getVolume()));

            /*if (voiceEntry.tracker() != null)
                voiceEntry.tracker().setUpdateVoiceListener(inf -> {

                });*/
        }
    }

    public synchronized void stop() {
        audioPlayer.stopTrack();
    }

    public int getMaxPrevisionLoadCount() {
        return 10;
    }

    private class CoolDownThread extends Thread {
        private final Runnable runnable;

        private CoolDownThread(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(1000);
                coolDownThread = null;
                runnable.run();
            } catch (InterruptedException ignored) {
            }
        }
    }
}
