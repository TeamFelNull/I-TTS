package dev.felnull.ttsvoice.data;

import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import com.google.common.collect.ImmutableList;
import dev.felnull.ttsvoice.Main;
import dev.felnull.ttsvoice.util.Json5Utils;

import java.util.List;

public record Config(int configVersion, List<String> botTokens, List<String> voiceVoxURLs, List<String> coeiroInkURLs,
                     List<String> sharevoxURLs, String voiceTextAPIKey, int cashTime, String ignoreRegex,
                     List<Long> inmDenyUser,
                     List<Long> cookieDenyUser, List<Long> adminRoles, List<Long> needAdminServers,
                     VoiceConfig voiceConfig) {

    public static Config createDefault() {
        return new Config(Main.CONFIG_VERSION, ImmutableList.of(), ImmutableList.of("http://localhost:50021"), ImmutableList.of("http://localhost:50031"), ImmutableList.of("http://127.0.0.1:50025"), "", 3, "(!|/|\\$|`).*", ImmutableList.of(), ImmutableList.of(), ImmutableList.of(939945132046827550L, 601000603354660864L), ImmutableList.of(930083398691733565L), VoiceConfig.createDefault());
    }

    public static Config of(JsonObject jo) {
        List<String> botTokens = Json5Utils.ofStringJsonArray(jo.get("BotToken"));
        List<String> voiceVoxURLs = Json5Utils.ofStringJsonArray(jo.get("VoiceVoxURL"));
        List<String> coeiroInkURLs = Json5Utils.ofStringJsonArray(jo.get("CoeiroInkURL"));
        List<String> sharevoxURLs = Json5Utils.ofStringJsonArray(jo.get("ShareVoxURL"));

        String voiceTextAPIKey = jo.get(String.class, "VoiceTextAPIKey");
        int cashTime = jo.getInt("CashTime", 3);
        String ignoreRegex = jo.get(String.class, "IgnoreRegex");

        List<Long> inmDenyUsers = Json5Utils.ofLongJsonArray(jo.get("InmDenyUser"), 0);
        List<Long> cookieDenyUsers = Json5Utils.ofLongJsonArray(jo.get("CookieDenyUser"), 0);

        List<Long> adminRoles = Json5Utils.ofLongJsonArray(jo.get("AdminRoles"), 0);
        List<Long> needAdminServers = Json5Utils.ofLongJsonArray(jo.get("NeedAdminServers"), 0);

        VoiceConfig vc = VoiceConfig.of(jo.getObject("VoiceConfig"));

        return new Config(jo.getInt("ConfigVersion", 0), botTokens, voiceVoxURLs, coeiroInkURLs, sharevoxURLs, voiceTextAPIKey, cashTime, ignoreRegex, inmDenyUsers, cookieDenyUsers, adminRoles, needAdminServers, vc);
    }

    public JsonObject toJson() {
        var jo = new JsonObject();
        jo.put("ConfigVersion", new JsonPrimitive(configVersion), "コンフィグバージョン(変更しないでください)");
        jo.put("BotToken", Json5Utils.toJsonArray(botTokens), "BOTトークン指定");
        jo.put("VoiceVoxURL", Json5Utils.toJsonArray(voiceVoxURLs), "VoiceVoxのURL指定");
        jo.put("CoeiroInkURL", Json5Utils.toJsonArray(coeiroInkURLs), "CoeiroInkのURL指定");
        jo.put("ShareVoxURL", Json5Utils.toJsonArray(sharevoxURLs), "ShareVoxのURL指定");
        jo.put("VoiceTextAPIKey", JsonPrimitive.of(voiceTextAPIKey), "VoiceTextのAPIキー指定");
        jo.put("CashTime", new JsonPrimitive(cashTime), "キャッシュを保存する期間(分)");
        jo.put("IgnoreRegex", JsonPrimitive.of(ignoreRegex), "無視する文字列");
        jo.put("InmDenyUser", Json5Utils.toJsonArray(inmDenyUser), "淫夢拒否ユーザー");
        jo.put("CookieDenyUser", Json5Utils.toJsonArray(cookieDenyUser), "クッキー☆拒否ユーザー");
        jo.put("AdminRoles", Json5Utils.toJsonArray(adminRoles), "管理可能なロール");
        jo.put("NeedAdminServers", Json5Utils.toJsonArray(needAdminServers), "管理ロール指定が必要なサーバー");
        jo.put("VoiceConfig", voiceConfig.toJson(), "読み上げタイプが有効かどうか");
        return jo;
    }

    public void check() {
        if (botTokens.isEmpty()) throw new IllegalStateException("Bot token is empty");
        if (voiceConfig.enableVoiceVox())
            if (voiceVoxURLs.isEmpty()) throw new IllegalStateException("VoiceVox url is empty");
        if (voiceConfig.enableCoeiroInk())
            if (coeiroInkURLs.isEmpty()) throw new IllegalStateException("CoeiroInk url is empty");
        if (voiceConfig.enableShareVox())
            if (sharevoxURLs.isEmpty()) throw new IllegalStateException("ShareVox url is empty");
        if (voiceConfig.enableVoiceText())
            if (voiceTextAPIKey.isEmpty()) throw new IllegalStateException("VoiceText api key is empty");
        if (cashTime < 0) throw new IllegalStateException("Cash time must be greater than or equal to 0");
    }
}
