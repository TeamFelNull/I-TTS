package dev.felnull.itts.core.voice.coeiroink;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * CoeiroinkのエンジンURL
 * COEIROINKは全てのエンドポイントが/v1/プレフィックスを使用する
 *
 * @param url ベースURL
 * @author MORIMORI0317
 */
public record CIURL(String url) {

    private static final String API_VERSION = "v1";

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

        String baseUrl = url.endsWith("/") ? url : url + "/";
        return new URL(baseUrl + path);
    }

    /**
     * v1 APIのURIを作成
     *
     * @param path パス (例: "speakers", "synthesis")
     * @return /v1/パスを含めたURI
     */
    public URI createURI(String path) {
        if (path.startsWith("/")) {
            throw new IllegalArgumentException("Do not start with /");
        }

        String baseUrl = url.endsWith("/") ? url : url + "/";
        return URI.create(baseUrl + API_VERSION + "/" + path);
    }
}
