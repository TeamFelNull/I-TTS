package dev.felnull.itts.core.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.felnull.itts.core.tts.saidtext.SaidText;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 読み込み済みの読み上げ音声
 *
 * @author MORIMORI0317
 */
public class LoadedSaidText {
    /**
     * 読み上げ音声
     */
    private final SaidText saidText;

    /**
     * 読み込み済みオーディオトラック
     */
    private final AudioTrack track;

    /**
     * 破棄時の処理
     */
    private final Runnable dispose;

    /**
     * すでに読み上げ終わったかどうか
     */
    private final AtomicBoolean alreadyUsed = new AtomicBoolean();

    /**
     * コンストラクタ
     *
     * @param saidText 読み上げ音声
     * @param track    オーディオトラック
     * @param dispose  破棄時の処理
     */
    public LoadedSaidText(SaidText saidText, AudioTrack track, Runnable dispose) {
        this.saidText = saidText;
        this.track = track;
        this.dispose = dispose;
    }

    public SaidText getSaidText() {
        return saidText;
    }

    public boolean isFailure() {
        return track == null;
    }

    /**
     * 破棄の処理を行う
     */
    public void dispose() {
        dispose.run();
    }

    /**
     * 読み上げ終わったかどうかを指定
     *
     * @param alreadyUsed 読み上げ終わったかどうか
     */
    public void setAlreadyUsed(boolean alreadyUsed) {
        this.alreadyUsed.set(alreadyUsed);
    }

    public boolean isAlreadyUsed() {
        return alreadyUsed.get();
    }

    public AudioTrack getTrack() {
        return track;
    }
}
