package dev.felnull.ttsvoice.core.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.felnull.ttsvoice.core.TTSVoiceRuntime;
import dev.felnull.ttsvoice.core.tts.saidtext.SaidText;
import net.dv8tion.jda.api.managers.AudioManager;
import org.apache.commons.lang3.tuple.Pair;

import java.util.concurrent.CompletableFuture;

public class VoiceAudioScheduler {
    private final AudioManager audioManager;
    private final VoiceAudioManager voiceAudioManager;
    private final AudioPlayer audioPlayer;

    public VoiceAudioScheduler(AudioManager audioManager, VoiceAudioManager voiceAudioManager) {
        this.audioManager = audioManager;
        this.voiceAudioManager = voiceAudioManager;
        this.audioPlayer = voiceAudioManager.getAudioPlayerManager().createPlayer();
        this.audioManager.setSendingHandler(new VoiceAudioHandler(audioPlayer));
    }

    public void dispose() {
        stop();
        this.audioManager.setSendingHandler(null);
    }

    public void test() {
        voiceAudioManager.getAudioPlayerManager().loadItem("https://cdn.discordapp.com/attachments/358878159615164416/1067424548582068294/anaruzigoku.mp3", new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                audioPlayer.playTrack(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {

            }

            @Override
            public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException exception) {

            }
        });
    }

    public Pair<CompletableFuture<LoadedSaidText>, Runnable> load(SaidText saidText) {
        Runnable stopRun = () -> {
        };

        return Pair.of(CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            return new LoadedSaidText(saidText);
        }, TTSVoiceRuntime.getInstance().getAsyncWorkerExecutor()), stopRun);
    }

    public void stop() {
        System.out.println("Say Stop");
    }

    public void play(LoadedSaidText loadedSaidText, Runnable playEndRun) {
        CompletableFuture.runAsync(() -> {
            System.out.println("Say Start:" + loadedSaidText.getSaidText());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
            System.out.println("Say End:" + loadedSaidText.getSaidText());

            loadedSaidText.setAlreadyUsed(true);
            playEndRun.run();
        }, TTSVoiceRuntime.getInstance().getAsyncWorkerExecutor());
    }
}
