package dev.felnull.ttsvoice.voice.reinoare.cookie;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.felnull.fnjl.util.FNStringUtil;
import dev.felnull.fnjl.util.FNURLUtil;
import dev.felnull.ttsvoice.Main;
import dev.felnull.ttsvoice.util.DiscordUtils;
import dev.felnull.ttsvoice.voice.VoiceType;
import dev.felnull.ttsvoice.voice.reinoare.ReinoareEntry;
import dev.felnull.ttsvoice.voice.reinoare.ReinoareManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;

public class CookieManager extends ReinoareManager {
    private static final CookieManager INSTANCE = new CookieManager();
    private final CookieVoiceType VOICE = new CookieVoiceType();

    public static CookieManager getInstance() {
        return INSTANCE;
    }


    @Override
    public List<ReinoareEntry> search(String text, int max) throws URISyntaxException, IOException {
        text = URLEncoder.encode(text, StandardCharsets.UTF_8);
        text = new URI(text).toASCIIString();
        var ret = FNURLUtil.getResponse(new URL(INC_URL + "/search?s=" + text + "&t=cookie_star&m=" + max));
        var jo = GSON.fromJson(ret, JsonObject.class);
        if (!jo.has("result"))
            return ImmutableList.of();
        var ja = jo.getAsJsonArray("result");
        ImmutableList.Builder<ReinoareEntry> builder = new ImmutableList.Builder<>();
        for (JsonElement entry : ja) {
            var ejo = entry.getAsJsonObject();
            var path = ejo.get("path").getAsString();
            //  if (!path.startsWith("/淫夢"))
            //     continue;
            builder.add(new CookieEntry(ejo.get("name").getAsString(), path, FNStringUtil.getUUIDFromStringNonThrow(ejo.get("uuid").getAsString())));
        }
        return builder.build();
    }

    @Override
    public List<ReinoareEntry> sort(List<ReinoareEntry> entries) {
        Comparator<ReinoareEntry> cp = Comparator.comparingInt(this::getMostPoint);
        cp = cp.reversed();
        return entries.stream().sorted(cp).toList();
    }

    public ReinoareEntry getMost(List<ReinoareEntry> entries) {
        if (entries.isEmpty()) return null;
        List<ReinoareEntry> sorted = sort(entries);
        int mostP = getMostPoint(sorted.get(0));
        List<ReinoareEntry> rets = sorted.stream().filter(n -> getMostPoint(n) == mostP).toList();
        return rets.get(RANDOM.nextInt(rets.size()));
    }

    private int getMostPoint(ReinoareEntry entry) {
        String[] paths = entry.getPath().split("/");
        int p = 0;
        for (String s : paths) {
            p += getMostNumber(s);
        }
        p += (getMostNumber(entry.getName()) * 2);
        return p;
    }

    private int getMostNumber(String text) {
        if (text.contains("★") || text.contains("RRM"))
            return 3;
        if (text.contains("ツイキャス") || text.contains("ANNYUI"))
            return -2;
        return 0;
    }

    @Override
    public VoiceType getVoice() {
        return VOICE;
    }

    @Override
    public boolean isEnable(long guildId) {
        return Main.getServerSaveData(guildId).isCookieMode(guildId) && !DiscordUtils.isNonAllowCookie(guildId);
    }

    @Override
    public InputStream getMP3(String name) {
        return super.getMP3("cookie/", name);
    }
}
