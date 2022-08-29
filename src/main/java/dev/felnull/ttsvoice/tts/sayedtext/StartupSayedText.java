package dev.felnull.ttsvoice.tts.sayedtext;

import dev.felnull.ttsvoice.Main;

import java.util.Objects;

public class StartupSayedText implements SayedText {
    private final String myName;
    private final String oldVersion;
    private final String newVersion;
    private final boolean restart;

    public StartupSayedText(String myName, String oldVersion, String newVersion, boolean restart) {
        this.myName = myName;
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
        this.restart = restart;
    }

    @Override
    public String getSayVoiceText() {
        String str = restart ? "再起動" : "起動";

        if (Main.isTest())
            str = "開発テストモードで" + str;

        if (oldVersion == null && newVersion == null)
            return myName + "が" + str + "しました";
        if (oldVersion == null)
            return myName + "が古いバージョンからバージョン" + newVersion + "へ更新され" + str + "しました";
        return myName + "がバージョン" + oldVersion + "からバージョン" + newVersion + "へ更新され" + str + "しました";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StartupSayedText that = (StartupSayedText) o;
        return Objects.equals(myName, that.myName) && Objects.equals(oldVersion, that.oldVersion) && Objects.equals(newVersion, that.newVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(myName, oldVersion, newVersion);
    }
}
