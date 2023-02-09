package dev.felnull.itts.core.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import dev.felnull.itts.core.ITTSRuntimeUse;
import dev.felnull.itts.core.tts.saidtext.SaidText;
import net.dv8tion.jda.api.managers.AudioManager;
import org.apache.commons.lang3.tuple.Pair;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

public class VoiceAudioScheduler extends AudioEventAdapter implements ITTSRuntimeUse {
    private final AudioManager audioManager;
    private final VoiceAudioManager voiceAudioManager;
    private final AudioPlayer audioPlayer;
    private final AtomicReference<Pair<LoadedSaidText, Runnable>> currentLoaded = new AtomicReference<>();
    private final long guildId;

    public VoiceAudioScheduler(AudioManager audioManager, VoiceAudioManager voiceAudioManager, long guildId) {
        this.audioManager = audioManager;
        this.voiceAudioManager = voiceAudioManager;
        this.audioPlayer = voiceAudioManager.getAudioPlayerManager().createPlayer();
        this.guildId = guildId;
        this.audioPlayer.addListener(this);
        this.audioManager.setSendingHandler(new VoiceAudioHandler(audioPlayer));
    }

    public void dispose() {
        stop();
        this.audioManager.setSendingHandler(null);
    }

    public CompletableFuture<LoadedSaidText> load(SaidText saidText) {
        String sayText = getDictionaryManager().applyDict(saidText.getText(), guildId);

        var vtl = saidText.getVoice().createVoiceTrackLoader(sayText);
        return vtl.load().thenApplyAsync(r -> new LoadedSaidText(saidText, r, vtl::dispose), getAsyncExecutor());
    }

    public void stop() {
        currentLoaded.set(null);
        audioPlayer.stopTrack();
    }

    public void play(LoadedSaidText loadedSaidText, Runnable playEndRun) {
        currentLoaded.set(Pair.of(loadedSaidText, playEndRun));
        audioPlayer.playTrack(loadedSaidText.getTrack());
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        var old = currentLoaded.getAndSet(null);
        if (old != null) {
            old.getLeft().setAlreadyUsed(true);
            old.getRight().run();
        }
    }


    public void test() {
        CompletableFuture.runAsync(() -> {
            AtomicReference<AudioTrack> trk = new AtomicReference<>();

            try {
                voiceAudioManager.getAudioPlayerManager().loadItem("./1e62b6541e6f335a222f3cc37df47cd8", new AudioLoadResultHandler() {
                    @Override
                    public void trackLoaded(AudioTrack track) {
                        trk.set(track);
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
                }).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            audioPlayer.playTrack(trk.get());


        }, getAsyncExecutor());
    }
}
