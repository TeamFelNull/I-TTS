package dev.felnull.ttsvoice.voice.reinoare.cookie;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.felnull.fnjl.util.FNDataUtil;
import dev.felnull.fnjl.util.FNStringUtil;
import dev.felnull.fnjl.util.FNURLUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class CookieManager {
    private static final CookieManager INSTANCE = new CookieManager();
    private static final Gson GSON = new Gson();
    private static final String INC_URL = "https://www.morimori0317.net/inc-sounds-search";
    private static final Random RANDOM = new Random();
    private final CookieVoiceType VOICE = new CookieVoiceType();

    public static CookieManager getInstance() {
        return INSTANCE;
    }

    public List<CookieEntry> search(String text) throws URISyntaxException, IOException {
        return search(text, 150);
    }

    public List<CookieEntry> search(String text, int max) throws URISyntaxException, IOException {
        text = URLEncoder.encode(text, StandardCharsets.UTF_8);
        text = new URI(text).toASCIIString();
        var ret = FNURLUtil.getResponse(new URL(INC_URL + "/search?s=" + text + "&t=cookie_star&m=" + max));
        var jo = GSON.fromJson(ret, JsonObject.class);
        if (!jo.has("result"))
            return ImmutableList.of();
        var ja = jo.getAsJsonArray("result");
        ImmutableList.Builder<CookieEntry> builder = new ImmutableList.Builder<>();
        for (JsonElement entry : ja) {
            var ejo = entry.getAsJsonObject();
            var path = ejo.get("path").getAsString();
            //  if (!path.startsWith("/淫夢"))
            //     continue;
            builder.add(new CookieEntry(ejo.get("name").getAsString(), path, FNStringUtil.getUUIDFromStringNonThrow(ejo.get("uuid").getAsString())));
        }
        return builder.build();
    }

    public List<CookieEntry> sort(List<CookieEntry> entries) {
        Comparator<CookieEntry> cp = Comparator.comparingInt(this::getMostPoint);
        cp = cp.reversed();
        return entries.stream().sorted(cp).toList();
    }

    public CookieEntry getMost(List<CookieEntry> entries) {
        if (entries.isEmpty()) return null;
        List<CookieEntry> sorted = sort(entries);
        int mostP = getMostPoint(sorted.get(0));
        List<CookieEntry> rets = sorted.stream().filter(n -> getMostPoint(n) == mostP).toList();
        return rets.get(RANDOM.nextInt(rets.size()));
    }

    private int getMostPoint(CookieEntry entry) {
        String[] paths = entry.path().split("/");
        int p = 0;
        for (String s : paths) {
            p += getMostNumber(s);
        }
        p += (getMostNumber(entry.name()) * 2);
        return p;
    }

    private int getMostNumber(String text) {
        if (text.contains("★") || text.contains("RRM"))
            return 3;
        if (text.contains("ツイキャス") || text.contains("ANNYUI"))
            return -2;
        return 0;
    }

    public String getFileURL(UUID uuid) {
        if (uuid == null) return null;
        return INC_URL + "/link/" + uuid;
    }

    public CookieVoiceType getVoice() {
        return VOICE;
    }

    public InputStream getJoinSound() {
        int num = RANDOM.nextInt(8) + 1;
        return getInmResource("join" + num + ".mp3");
    }

    public InputStream getLeaveSound() {
        int num = RANDOM.nextInt(5) + 1;
        return getInmResource("leave" + num + ".mp3");
    }

    private InputStream getInmResource(String name) {
        return FNDataUtil.resourceExtractor(CookieManager.class, "inm/" + name);
    }
}
