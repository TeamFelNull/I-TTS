package dev.felnull.itts.core.voice.voicetext;

/**
 * VoiceTextの話者
 *
 * @author MORIMORI0317
 */
public enum VoiceTextSpeaker {

    /**
     * Show
     */
    SHOW("show", "男性"),

    /**
     * Haruka
     */
    HARUKA("haruka", "女性"),

    /**
     * Hikari
     */
    HIKARI("hikari", "女性"),

    /**
     * Takeru
     */
    TAKERU("takeru", "男性"),

    /**
     * Santa
     */
    SANTA("santa", "サンタ"),

    /**
     * Kuma
     */
    BEAR("bear", "凶暴なクマ");

    /**
     * 話者ID
     */
    private final String id;

    /**
     * 話者名
     */
    private final String name;

    VoiceTextSpeaker(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
