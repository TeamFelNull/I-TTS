package dev.felnull.ttsvoice.tts.sayedtext;

public class StartupSayedText implements SayedText {
    private final String myName;
    private final String oldVersion;
    public final String newVersion;

    public StartupSayedText(String myName) {
        this(myName, null, null);
    }

    public StartupSayedText(String myName, String oldVersion, String newVersion) {
        this.myName = myName;
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
    }

    @Override
    public String getSayVoiceText() {
        if (oldVersion == null && newVersion == null)
            return myName + "が起動しました";
        if (oldVersion == null)
            return myName + "が古いバージョンからバージョン" + newVersion + "へ更新され起動しました";
        return myName + "がバージョン" + oldVersion + "からバージョン" + newVersion + "へ更新され起動しました";
    }
}
