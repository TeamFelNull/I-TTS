package dev.felnull.itts.core.voice.coeiroink;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * CoeiroinkのエンジンURL
 *
 * @param url URL
 * @author MORIMORI0317
 */
public record CIURL(String url) {

    /**
     * URLを作成
     *
     * @param path パス
     * @return パスを含めたURL
     * @throws MalformedURLException URL生成例外
     */
    public URL createURL(String path) throws MalformedURLException {
        if (path.startsWith("/")) {
            throw new IllegalArgumentException("Do not start with /");
        }

        String ur = url;

        if (!ur.endsWith("/")) {
            ur += "/";
        }

        return new URL(ur + path);
    }

    /**
     * URIを作成
     *
     * @param path パス
     * @return パスを含めたURI
     */
    public URI createURI(String path) {
        if (path.startsWith("/")) {
            throw new IllegalArgumentException("Do not start with /");
        }

        String ur = url;

        if (!ur.endsWith("/")) {
            ur += "/";
        }

        // TODO : v1/の挿入方法を改善する
        return URI.create(ur + "v1/" + path);
    }
}
