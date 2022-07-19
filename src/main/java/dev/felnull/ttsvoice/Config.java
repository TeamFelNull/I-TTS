package dev.felnull.ttsvoice;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;

public record Config(List<String> botTokens, List<String> voiceVoxURLs, List<String> coeiroInkURLs,
                     String voiceTextAPIKey, int cashTime,
                     String ignoreRegex,
                     List<Long> inmDenyUser, List<Long> adminRoles, List<Long> needAdminServers) {

    public static Config of(JsonObject jo) {

        ImmutableList.Builder<String> botTokensBuilder = new ImmutableList.Builder<>();
        var btje = jo.get("BotToken");
        if (btje.isJsonPrimitive() && btje.getAsJsonPrimitive().isString()) {
            botTokensBuilder.add(btje.getAsString());
        } else if (btje.isJsonArray()) {
            var btja = btje.getAsJsonArray();
            for (JsonElement je : btja) {
                botTokensBuilder.add(je.getAsString());
            }
        }

        ImmutableList.Builder<String> voiceVoxURLsBuilder = new ImmutableList.Builder<>();
        var vvuje = jo.get("VoiceVoxURL");
        if (vvuje.isJsonPrimitive() && vvuje.getAsJsonPrimitive().isString()) {
            voiceVoxURLsBuilder.add(vvuje.getAsString());
        } else if (vvuje.isJsonArray()) {
            var vvja = vvuje.getAsJsonArray();
            for (JsonElement je : vvja) {
                voiceVoxURLsBuilder.add(je.getAsString());
            }
        }

        ImmutableList.Builder<String> coeiroInkURLsBuilder = new ImmutableList.Builder<>();
        var ciuje = jo.get("CoeiroInkURL");
        if (ciuje.isJsonPrimitive() && ciuje.getAsJsonPrimitive().isString()) {
            coeiroInkURLsBuilder.add(ciuje.getAsString());
        } else if (ciuje.isJsonArray()) {
            var cija = ciuje.getAsJsonArray();
            for (JsonElement je : cija) {
                coeiroInkURLsBuilder.add(je.getAsString());
            }
        }

        ImmutableList.Builder<Long> inmDenyBuilder = new ImmutableList.Builder<>();
        var idbja = jo.getAsJsonArray("InmDenyUser");
        for (JsonElement entry : idbja) {
            inmDenyBuilder.add(entry.getAsLong());
        }

        ImmutableList.Builder<Long> adminRolesBuilder = new ImmutableList.Builder<>();
        var arja = jo.getAsJsonArray("AdminRoles");
        for (JsonElement entry : arja) {
            adminRolesBuilder.add(entry.getAsLong());
        }

        ImmutableList.Builder<Long> needAdminServersBuilder = new ImmutableList.Builder<>();
        var naja = jo.getAsJsonArray("NeedAdminServers");
        for (JsonElement entry : naja) {
            needAdminServersBuilder.add(entry.getAsLong());
        }

        return new Config(botTokensBuilder.build(), voiceVoxURLsBuilder.build(), coeiroInkURLsBuilder.build(), jo.get("VoiceTextAPIKey").getAsString(), jo.get("CashTime").getAsInt(), jo.get("IgnoreRegex").getAsString(), inmDenyBuilder.build(), adminRolesBuilder.build(), needAdminServersBuilder.build());
    }

    public static Config createDefault() {
        return new Config(ImmutableList.of(), ImmutableList.of("http://localhost:50021"), ImmutableList.of("http://127.0.0.1:50031"), "", 3, "(!|/|\\$|`).*", ImmutableList.of(), ImmutableList.of(939945132046827550L, 601000603354660864L), ImmutableList.of(930083398691733565L));
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

    public JsonObject toJson() {
        var jo = new JsonObject();

        var btja = new JsonArray();
        for (String botToken : botTokens) {
            btja.add(botToken);
        }

        jo.add("BotToken", btja);

        var vvja = new JsonArray();
        for (String voiceVoxURL : voiceVoxURLs) {
            vvja.add(voiceVoxURL);
        }

        jo.add("VoiceVoxURL", vvja);

        var cija = new JsonArray();
        for (String coeiroInkURL : coeiroInkURLs) {
            cija.add(coeiroInkURL);
        }
        jo.add("CoeiroInkURL", cija);

        jo.addProperty("VoiceTextAPIKey", voiceTextAPIKey);
        jo.addProperty("CashTime", cashTime);
        jo.addProperty("IgnoreRegex", ignoreRegex);

        var idja = new JsonArray();
        for (Long entry : inmDenyUser) {
            idja.add(entry);
        }
        jo.add("InmDenyUser", idja);

        var arja = new JsonArray();
        for (Long entry : adminRoles) {
            arja.add(entry);
        }
        jo.add("AdminRoles", arja);

        var naja = new JsonArray();
        for (Long entry : needAdminServers) {
            naja.add(entry);
        }
        jo.add("NeedAdminServers", naja);

        return jo;
    }
}
