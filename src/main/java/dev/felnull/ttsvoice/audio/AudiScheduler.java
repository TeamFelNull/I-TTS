package dev.felnull.ttsvoice.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import dev.felnull.ttsvoice.Main;
import dev.felnull.ttsvoice.tts.TTSManager;

public class AudiScheduler extends AudioEventAdapter {
    private final AudioPlayer player;
    private final long guildId;
    private boolean loading;
    private CoolDownThread coolDownThread;

    public AudiScheduler(AudioPlayer player, long guildId) {
        this.player = player;
        this.player.addListener(this);
        var guild = Main.JDA.getGuildById(guildId);
        guild.getAudioManager().setSendingHandler(new AudioPlayerSendHandler(player));
        this.guildId = guildId;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        startCoolDown();
    }

    private void startCoolDown() {
        if (coolDownThread != null) {
            coolDownThread.interrupt();
        }
        coolDownThread = new CoolDownThread();
        coolDownThread.start();
    }

    public void play(AudioTrack track) {
        player.startTrack(track, false);
    }

    public boolean isLoadingOrPlaying() {
        return (coolDownThread != null && coolDownThread.isAlive()) || player.getPlayingTrack() != null || loading;
    }

    public synchronized boolean next() {
        var tm = TTSManager.getInstance();
        var next = tm.getTTSQueue(guildId).poll();
        if (next == null) return false;
        loading = true;
        var file = tm.getVoiceFile(next);
        if (file == null) {
            startCoolDown();
            loading = false;
            return true;
        }
        synchronized (tm.getVoiceCash()) {
            var sc = VoiceAudioPlayerManager.getInstance().getScheduler(guildId);
            AudioTrack track;
            try {
                track = VoiceAudioPlayerManager.getInstance().loadFile(file);
            } catch (Exception ex) {
                startCoolDown();
                loading = false;
                return true;
            }
            sc.play(track);
        }
        loading = false;
        return true;
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
