package dev.felnull.ttsvoice.voice.reinoare.cookie;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.felnull.fnjl.util.FNDataUtil;
import dev.felnull.fnjl.util.FNStringUtil;
import dev.felnull.fnjl.util.FNURLUtil;
import dev.felnull.ttsvoice.voice.reinoare.ReinoareManager;
import dev.felnull.ttsvoice.voice.reinoare.inm.INMManager;

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

public class CookieManager extends ReinoareManager {
    private static final CookieManager INSTANCE = new CookieManager();
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

    public CookieVoiceType getVoice() {
        return VOICE;
    }
    @Override
    public InputStream getMP3(String name) {
        return super.getMP3("cookie/", name);
    }
}
