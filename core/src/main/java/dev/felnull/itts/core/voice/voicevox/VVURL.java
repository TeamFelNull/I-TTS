package dev.felnull.itts.core.voice.voicevox;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public record VVURL(String url) {
    public URL createURL(String path) throws MalformedURLException {
        if (path.startsWith("/"))
            throw new IllegalArgumentException("Do not start with /");

        var ur = url;
        if (!ur.endsWith("/"))
            ur += "/";

        return new URL(ur + path);
    }

    public URI createURI(String path) {
        if (path.startsWith("/"))
            throw new IllegalArgumentException("Do not start with /");

        var ur = url;
        if (!ur.endsWith("/"))
            ur += "/";

        return URI.create(ur + path);
    }
}
