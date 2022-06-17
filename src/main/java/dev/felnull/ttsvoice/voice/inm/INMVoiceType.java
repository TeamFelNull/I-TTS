package dev.felnull.ttsvoice.voice.inm;

import dev.felnull.fnjl.util.FNURLUtil;
import dev.felnull.ttsvoice.tts.IVoiceType;

import java.io.InputStream;
import java.net.URL;

public class INMVoiceType implements IVoiceType {
    @Override
    public String getTitle() {
        return "淫夢";
    }

    @Override
    public String getId() {
        return "inm";
    }

    @Override
    public InputStream getSound(String text) throws Exception {
        var im = INMManager.getInstance();
        var ret = im.search(text);
        var most = im.getMost(ret);
        if (most == null)
            return null;
        return FNURLUtil.getStream(new URL(most.getURL()));
    }
}
