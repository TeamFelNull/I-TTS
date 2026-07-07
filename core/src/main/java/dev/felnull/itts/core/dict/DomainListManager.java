package dev.felnull.itts.core.dict;

import dev.felnull.itts.core.ITTSBaseManager;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * TLDリストマネージャー
 **/

public class DomainListManager implements ITTSBaseManager {

    /**
     *  取得先URL
     */
    private static final URI IANA_DATA_URI = URI.create("https://data.iana.org/TLD/tlds-alpha-by-domain.txt");

    /**
     * 読み込み済みTLDリスト
     */
    private Pattern domainPattern;

    @Override
    public @NotNull CompletableFuture<?> init() {
        return CompletableFuture.runAsync(() -> {
            try {
                HttpClient hc = getNetworkManager().getHttpClient();
                HttpRequest request = HttpRequest.newBuilder().uri(IANA_DATA_URI).GET().build();

                HttpResponse<String> res = hc.send(request, HttpResponse.BodyHandlers.ofString());

                String processedDomains = res.body().lines()
                    .filter(line -> !line.startsWith("#"))
                    .filter(list -> !list.isBlank())
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .map(Pattern::quote)
                    .collect(Collectors.joining("|"));

                String regex = "\\b[a-zA-Z0-9.-]+\\.(?i:" + processedDomains + ")\\b";
                domainPattern = Pattern.compile(regex);
            } catch (IOException | InterruptedException e) {
                getITTSLogger().error("failed to get domain list.");
            }
        }, getAsyncExecutor());
    }

    /**
     *  読み込み済みTLDゲッター
     *
     * @return ドメイン検知正規表現
     */
    public @Nullable Pattern getPattern() {
        return domainPattern;
    }
}
