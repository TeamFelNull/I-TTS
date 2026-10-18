package dev.felnull.itts.core.voice.openai;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
import dev.felnull.itts.core.config.voicetype.OpenAiTtsConfig;
import dev.felnull.itts.core.voice.VoiceType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ローカルHTTPサーバーでOpenAI互換TTSの通信と音声選択を検証する
 */
class OpenAiTtsManagerTest {
    private HttpServer server;
    private HttpClient client;
    private final AtomicReference<JsonObject> requestBody = new AtomicReference<>();
    private final AtomicReference<String> authorization = new AtomicReference<>();
    private final AtomicReference<String> method = new AtomicReference<>();
    private final AtomicReference<String> requestContentType = new AtomicReference<>();
    private int status = 200;
    private String contentType = "audio/wav";

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/audio/speech", exchange -> {
            method.set(exchange.getRequestMethod());
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            requestContentType.set(exchange.getRequestHeaders().getFirst("Content-Type"));
            requestBody.set(JsonParser.parseString(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject());
            if (contentType != null) {
                exchange.getResponseHeaders().set("Content-Type", contentType);
            }
            byte[] audio = wav();
            exchange.sendResponseHeaders(status, audio.length);
            exchange.getResponseBody().write(audio);
            exchange.close();
        });
        server.start();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        client.close();
        server.stop(0);
    }

    private OpenAiTtsConfig config(String apiKey, boolean enabled) {
        return new OpenAiTtsConfig() {
            @Override
            public boolean isEnable() {
                return enabled;
            }

            @Override
            public String getBaseUrl() {
                return "http://127.0.0.1:" + server.getAddress().getPort() + "/v1/";
            }

            @Override
            public String getApiKey() {
                return apiKey;
            }

            @Override
            public String getModel() {
                return "custom-model";
            }

            @Override
            public List<String> getVoices() {
                return List.of("speaker", "other", "speaker");
            }
        };
    }

    private OpenAiTtsManager manager(String apiKey, boolean enabled) {
        return new OpenAiTtsManager(() -> config(apiKey, enabled), () -> client);
    }

    private static byte[] wav() {
        ByteBuffer buffer = ByteBuffer.allocate(4844).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put("RIFF".getBytes(StandardCharsets.US_ASCII)).putInt(4836);
        buffer.put("WAVEfmt ".getBytes(StandardCharsets.US_ASCII)).putInt(16);
        buffer.putShort((short) 1).putShort((short) 1).putInt(24000).putInt(48000);
        buffer.putShort((short) 2).putShort((short) 16);
        buffer.put("data".getBytes(StandardCharsets.US_ASCII)).putInt(4800);
        return buffer.array();
    }

    @Test
    void sendsJsonAndBearerAuthenticationToNormalizedEndpoint() throws Exception {
        String text = "日本語の\"引用\"と\n改行";
        try (InputStream stream = manager("test-key", true).openVoiceStream("speaker", text)) {
            assertArrayEquals(wav(), stream.readAllBytes());
        }
        assertEquals("POST", method.get());
        assertEquals("application/json", requestContentType.get());
        assertEquals("Bearer test-key", authorization.get());
        assertEquals("custom-model", requestBody.get().get("model").getAsString());
        assertEquals("speaker", requestBody.get().get("voice").getAsString());
        assertEquals(text, requestBody.get().get("input").getAsString());
        assertEquals("wav", requestBody.get().get("response_format").getAsString());
    }

    @Test
    void supportsUnauthenticatedBinaryResponse() throws Exception {
        contentType = "application/octet-stream; charset=binary";
        try (InputStream stream = manager("", true).openVoiceStream("speaker", "hello");
             AudioInputStream audio = AudioSystem.getAudioInputStream(new BufferedInputStream(stream))) {
            assertEquals(24000, audio.getFormat().getSampleRate());
            assertEquals(4800, audio.readAllBytes().length);
        }
        assertNull(authorization.get());
    }

    @Test
    void rejectsHttpErrorsEvenWhenContentTypeIsAudio() {
        status = 401;
        IOException error = assertThrows(IOException.class, () -> manager("", true).openVoiceStream("speaker", "hello"));
        assertTrue(error.getMessage().contains("401"));
    }

    @Test
    void rejectsJsonAndMissingContentType() {
        contentType = "application/json";
        assertThrows(IOException.class, () -> manager("", true).openVoiceStream("speaker", "hello"));
        contentType = null;
        assertThrows(IOException.class, () -> manager("", true).openVoiceStream("speaker", "hello"));
    }

    @Test
    void exposesConfiguredVoicesAndDisablesRequestsWhenUnavailable() {
        OpenAiTtsManager enabled = manager("", true);
        List<VoiceType> types = enabled.getVoiceTypes();
        assertEquals(List.of("openai_tts:speaker", "openai_tts:other"), types.stream().map(VoiceType::getId).toList());
        assertSame(enabled.getCategory(), types.getFirst().getCategory());
        assertEquals("openai_tts", enabled.getCategory().getId());
        assertEquals(4096, types.getFirst().createVoice(1, 2).getReadLimit());
        assertThrows(IOException.class, () -> enabled.openVoiceStream("unknown", "hello"));
        OpenAiTtsManager disabled = manager("", false);
        assertFalse(disabled.getCategory().isAvailable());
        assertTrue(disabled.getVoiceTypes().isEmpty());
        assertThrows(IOException.class, () -> disabled.openVoiceStream("speaker", "hello"));
        assertNull(requestBody.get());
    }

    @Test
    void separatesCacheByModelAndEndpoint() {
        OpenAiTtsManager original = manager("", true);
        OpenAiTtsManager otherModel = new OpenAiTtsManager(() -> new OpenAiTtsConfig() {
            @Override
            public String getBaseUrl() {
                return config("", true).getBaseUrl();
            }

            @Override
            public String getModel() {
                return "other-model";
            }
        }, () -> client);
        OpenAiTtsManager otherEndpoint = new OpenAiTtsManager(() -> new OpenAiTtsConfig() {
            @Override
            public String getModel() {
                return "custom-model";
            }
        }, () -> client);
        assertNotEquals(original.getCacheIdentity(), otherModel.getCacheIdentity());
        assertNotEquals(original.getCacheIdentity(), otherEndpoint.getCacheIdentity());
    }

    @Test
    @SuppressWarnings("unchecked")
    void closesRejectedResponseStream() throws Exception {
        HttpClient mockClient = mock(HttpClient.class);
        HttpResponse<InputStream> response = mock(HttpResponse.class);
        InputStream body = mock(InputStream.class);
        when(response.body()).thenReturn(body);
        when(response.statusCode()).thenReturn(500);
        when(response.headers()).thenReturn(HttpHeaders.of(Map.of("Content-Type", List.of("application/json")), (_, _) -> true));
        when(mockClient.<InputStream>send(any(), any())).thenReturn(response);
        OpenAiTtsManager manager = new OpenAiTtsManager(() -> config("", true), () -> mockClient);
        assertThrows(IOException.class, () -> manager.openVoiceStream("speaker", "hello"));
        verify(body).close();
    }

    @Test
    void reportsSynthesisTimeout() throws Exception {
        HttpClient mockClient = mock(HttpClient.class);
        HttpTimeoutException timeout = new HttpTimeoutException("test timeout");
        when(mockClient.<InputStream>send(any(), any())).thenThrow(timeout);
        OpenAiTtsManager manager = new OpenAiTtsManager(() -> config("", true), () -> mockClient);
        IOException error = assertThrows(IOException.class, () -> manager.openVoiceStream("speaker", "hello"));
        assertSame(timeout, error.getCause());
        assertTrue(error.getMessage().contains("30 seconds"));
    }
}
