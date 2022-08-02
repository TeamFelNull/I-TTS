package dev.felnull.ttsvoice;

import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import com.google.common.collect.ImmutableList;
import dev.felnull.ttsvoice.util.Json5Utils;

import java.util.List;

public record Config(List<String> botTokens, List<String> voiceVoxURLs, List<String> coeiroInkURLs,
                     String voiceTextAPIKey, int cashTime, String ignoreRegex, List<Long> inmDenyUser,
                     List<Long> cookieDenyUser, List<Long> adminRoles, List<Long> needAdminServers) {

    public static Config createDefault() {
        return new Config(ImmutableList.of(), ImmutableList.of("http://localhost:50021"), ImmutableList.of("http://127.0.0.1:50031"), "", 3, "(!|/|\\$|`).*", ImmutableList.of(), ImmutableList.of(), ImmutableList.of(939945132046827550L, 601000603354660864L), ImmutableList.of(930083398691733565L));
    }

    public static Config of(JsonObject jo) {
        List<String> botTokens = Json5Utils.ofStringJsonArray(Json5Utils.getJsonArray(jo, "BotToken"));
        List<String> voiceVoxURLs = Json5Utils.ofStringJsonArray(Json5Utils.getJsonArray(jo, "VoiceVoxURL"));
        List<String> coeiroInkURLs = Json5Utils.ofStringJsonArray(Json5Utils.getJsonArray(jo, "CoeiroInkURL"));

        String voiceTextAPIKey = jo.get(String.class, "VoiceTextAPIKey");
        int cashTime = jo.getInt("CashTime", 3);
        String ignoreRegex = jo.get(String.class, "IgnoreRegex");

        List<Long> inmDenyUsers = Json5Utils.ofLongJsonArray(Json5Utils.getJsonArray(jo, "InmDenyUser"), 0);
        List<Long> cookieDenyUsers = Json5Utils.ofLongJsonArray(Json5Utils.getJsonArray(jo, "CookieDenyUser"), 0);

        List<Long> adminRoles = Json5Utils.ofLongJsonArray(Json5Utils.getJsonArray(jo, "AdminRoles"), 0);
        List<Long> needAdminServers = Json5Utils.ofLongJsonArray(Json5Utils.getJsonArray(jo, "NeedAdminServers"), 0);

        return new Config(botTokens, voiceVoxURLs, coeiroInkURLs, voiceTextAPIKey, cashTime, ignoreRegex, inmDenyUsers, cookieDenyUsers, adminRoles, needAdminServers);
    }

    public JsonObject toJson() {
        var jo = new JsonObject();

        jo.put("BotToken", Json5Utils.toJsonArray(botTokens));
        jo.put("VoiceVoxURL", Json5Utils.toJsonArray(voiceVoxURLs));
        jo.put("CoeiroInkURL", Json5Utils.toJsonArray(coeiroInkURLs));
        jo.put("VoiceTextAPIKey", JsonPrimitive.of(voiceTextAPIKey));
        jo.put("CashTime", new JsonPrimitive(cashTime));
        jo.put("IgnoreRegex", JsonPrimitive.of(ignoreRegex));
        jo.put("InmDenyUser", Json5Utils.toJsonArray(inmDenyUser));
        jo.put("CookieDenyUser", Json5Utils.toJsonArray(cookieDenyUser));
        jo.put("AdminRoles", Json5Utils.toJsonArray(adminRoles));
        jo.put("NeedAdminServers", Json5Utils.toJsonArray(needAdminServers));

        return jo;
    }

    public void check() {
        if (botTokens.isEmpty())
            throw new IllegalStateException("Bot token is empty");
        if (voiceVoxURLs.isEmpty())
            throw new IllegalStateException("VoiceVox url is empty");
        if (coeiroInkURLs.isEmpty())
            throw new IllegalStateException("CoeiroInk url is empty");
        if (voiceTextAPIKey.isEmpty())
            throw new IllegalStateException("VoiceText api key is empty");
        if (cashTime < 0)
            throw new IllegalStateException("Cash time must be greater than or equal to 0");
    }
}
