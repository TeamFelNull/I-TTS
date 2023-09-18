package dev.felnull.itts.core;

import com.google.common.base.Suppliers;
import org.jetbrains.annotations.NotNull;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;

/**
 * ネットワーク接続関係の処理を行うクラス
 *
 * @author MORIMORI0317
 */
public class ITTSNetworkManager {

    /**
     * メモ化されたHttpClient
     */
    private final Supplier<HttpClient> httpClient = Suppliers.memoize(ITTSNetworkManager::createHttpClient);

    /**
     * HTTPSクライアントを生成<br/>
     * 大量のスレッドによるOutOfMemoryを防止するため、接続のたびにクライアントを生成しないでください。
     *
     * @return JavaのHttpClient
     */
    @NotNull
    private static HttpClient createHttpClient() {
        ITTSRuntime runtime = ITTSRuntime.getInstance();

        HttpClient.Builder builder = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.of(3, ChronoUnit.SECONDS))
                .executor(runtime.getHttpWorkerExecutor());

        return builder.build();
    }

    /**
     * 生成済みのHttpClientを取得
     *
     * @return JavaのHttpClient
     */
    @NotNull
    public HttpClient getHttpClient() {
        return httpClient.get();
    }
}
