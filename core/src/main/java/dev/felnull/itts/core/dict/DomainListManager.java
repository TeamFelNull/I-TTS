package dev.felnull.itts.core.dict;

import dev.felnull.itts.core.ITTSBaseManager;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
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
     * TLDリスト取得のタイムアウト
     */
    private static final Duration DOMAIN_LIST_TIMEOUT = Duration.ofSeconds(10);

    /**
     * TLDリスト取得の最大試行回数
     */
    private static final int DOMAIN_LIST_MAX_ATTEMPTS = 3;

    /**
     * 読み込み済みTLDリスト
     */
    private Pattern domainPattern;

    @Override
    public @NotNull CompletableFuture<?> init() {
        return CompletableFuture.runAsync(() -> {
            try {
                loadDomainListWithRetry();
            } catch (IOException | InterruptedException e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                getITTSLogger().error("Failed to get domain list from {}", IANA_DATA_URI, e);
            }
        }, getAsyncExecutor());
    }

    private void loadDomainListWithRetry() throws IOException, InterruptedException {
        IOException lastException = null;

        for (int attempt = 1; attempt <= DOMAIN_LIST_MAX_ATTEMPTS; attempt++) {
            try {
                loadDomainList();
                return;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw e;
            } catch (IOException e) {
                lastException = e;
                getITTSLogger().warn("Failed to get domain list from {} ({}/{})", IANA_DATA_URI, attempt, DOMAIN_LIST_MAX_ATTEMPTS, e);
            }
        }

        throw new IOException("Failed to get domain list after " + DOMAIN_LIST_MAX_ATTEMPTS + " attempts", lastException);
    }

    private void loadDomainList() throws IOException, InterruptedException {
        HttpClient hc = getNetworkManager().getHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(IANA_DATA_URI)
                .timeout(DOMAIN_LIST_TIMEOUT)
                .GET()
                .build();

        HttpResponse<String> res = hc.send(request, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() != 200) {
            throw new IOException("Domain list API error: HTTP " + res.statusCode());
        }

        String processedDomains = res.body().lines()
                .filter(line -> !line.startsWith("#"))
                .filter(list -> !list.isBlank())
                .map(String::trim)
                .map(String::toLowerCase)
                .map(Pattern::quote)
                .collect(Collectors.joining("|"));

        String regex = "\\b[a-zA-Z0-9.-]+\\.(?i:" + processedDomains + ")\\b";
        domainPattern = Pattern.compile(regex);
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
