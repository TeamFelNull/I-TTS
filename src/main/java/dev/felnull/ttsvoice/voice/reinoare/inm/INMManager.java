package dev.felnull.ttsvoice.voice.reinoare.inm;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.felnull.fnjl.util.FNStringUtil;
import dev.felnull.fnjl.util.FNURLUtil;
import dev.felnull.ttsvoice.Main;
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

public class INMManager extends ReinoareManager {
    private static final INMManager INSTANCE = new INMManager();
    private final INMVoiceType VOICE = new INMVoiceType();

    public static INMManager getInstance() {
        return INSTANCE;
    }

    @Override
    public List<ReinoareEntry> search(String text, int max) throws URISyntaxException, IOException {
        text = URLEncoder.encode(text, StandardCharsets.UTF_8);
        text = new URI(text).toASCIIString();
      //  var ret = FNURLUtil.getResponse(new URL(INC_URL + "/search?s=" + text + "&t=inm&st=learn&m=" + max));
        var ret = FNURLUtil.getResponse(new URL(INC_URL + "/search?s=" + text + "&t=inm&m=" + max));
        var jo = GSON.fromJson(ret, JsonObject.class);
        if (!jo.has("result"))
            return ImmutableList.of();
        var ja = jo.getAsJsonArray("result");
        ImmutableList.Builder<ReinoareEntry> builder = new ImmutableList.Builder<>();
        for (JsonElement entry : ja) {
            var ejo = entry.getAsJsonObject();
            var path = ejo.get("path").getAsString();
            builder.add(new INMEntry(ejo.get("name").getAsString(), path, FNStringUtil.getUUIDFromStringNonThrow(ejo.get("uuid").getAsString())));
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

    @Override
    public VoiceType getVoice() {
        return VOICE;
    }

    @Override
    public boolean isEnable(long guildId) {
        return Main.getServerSaveData(guildId).isInmMode(guildId);
    }

    @Override
    public InputStream getMP3(String name) {
        return super.getMP3("inm/", name);
    }
}
