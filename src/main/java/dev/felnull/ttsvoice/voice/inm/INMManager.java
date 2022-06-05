package dev.felnull.ttsvoice.voice.inm;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.felnull.fnjl.util.FNStringUtil;
import dev.felnull.fnjl.util.FNURLUtil;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class INMManager {
    private static final INMManager INSTANCE = new INMManager();
    private static final Gson GSON = new Gson();
    private static final String INC_URL = "https://www.morimori0317.net/inc-sounds-search";
    private static final Random RANDOM = new Random();
    private final INMVoiceType VOICE = new INMVoiceType();

    public static INMManager getInstance() {
        return INSTANCE;
    }

    public List<INMEntry> search(String text) throws URISyntaxException, IOException {
        return search(text, 150);
    }

    public List<INMEntry> search(String text, int max) throws URISyntaxException, IOException {
        text = URLEncoder.encode(text, StandardCharsets.UTF_8);
        text = new URI(text).toASCIIString();
        var ret = FNURLUtil.getResponse(new URL(INC_URL + "/search?s=" + text + "&t=inm&m=" + max));
        var jo = GSON.fromJson(ret, JsonObject.class);
        if (!jo.has("result"))
            return ImmutableList.of();
        var ja = jo.getAsJsonArray("result");
        ImmutableList.Builder<INMEntry> builder = new ImmutableList.Builder<>();
        for (JsonElement entry : ja) {
            var ejo = entry.getAsJsonObject();
            var path = ejo.get("path").getAsString();
            //  if (!path.startsWith("/淫夢"))
            //     continue;
            builder.add(new INMEntry(ejo.get("name").getAsString(), path, FNStringUtil.getUUIDFromStringNonThrow(ejo.get("uuid").getAsString())));
        }
        return builder.build();
    }

    public List<INMEntry> sort(List<INMEntry> entries) {
        Comparator<INMEntry> cp = Comparator.comparingInt(this::getMostPoint);
        cp = cp.reversed();
        return entries.stream().sorted(cp).toList();
    }

    public INMEntry getMost(List<INMEntry> entries) {
        if (entries.isEmpty()) return null;
        List<INMEntry> sorted = sort(entries);
        int mostP = getMostPoint(sorted.get(0));
        List<INMEntry> rets = sorted.stream().filter(n -> getMostPoint(n) == mostP).toList();
        return rets.get(RANDOM.nextInt(rets.size()));
    }

    private int getMostPoint(INMEntry entry) {
        String[] paths = entry.path().split("/");
        int p = 0;
        for (String s : paths) {
            p += getMostNumber(s);
        }
        p += (getMostNumber(entry.name()) * 2);
        return p;
    }

    private int getMostNumber(String text) {
        if (text.contains("野獣先輩") || text.contains("４章") || text.contains("野獣インタビュー"))
            return 3;
        if (text.contains("オークション") || text.contains("レストラン") || text.contains("現場監督") || text.contains("いなり") || text.contains("課長") || text.contains("ラビリンス") || text.contains("サムソン") || text.contains("野獣") || text.contains("鈴木") || text.contains("先輩") || text.contains("ゆうさく") || text.contains("KMR") || text.contains("MUR") || text.contains("木村") || text.contains("三浦"))
            return 2;
        if (text.contains("清野") || text.contains("関西") || text.contains("アツイ") || text.contains("肉") || text.contains("土方") || text.contains("相撲部") || text.contains("空手部") || text.contains("真夏の夜の淫夢") || text.contains("章") || text.contains("インタビュー"))
            return 1;
        if (text.contains("マイナー") || text.contains("偽") || text.contains("風評") || text.contains("ツイキャス") || text.contains("両成敗") || text.contains("サイヤ") || text.contains("最強雄筋肉") || text.contains("雄") || text.contains("両方"))
            return -2;
        return 0;
    }

    public String getFileURL(UUID uuid) {
        if (uuid == null) return null;
        return INC_URL + "/link/" + uuid;
    }

    public INMVoiceType getVoice() {
        return VOICE;
    }
}
