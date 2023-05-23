package dev.felnull.itts.savedata;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.felnull.itts.core.savedata.ServerUserData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerUsersData extends SaveDataBase {
    private final Map<Long, ServerUserDataImpl> serverUserData = new ConcurrentHashMap<>();

    @Override
    public String getName() {
        return "Server Users Data";
    }

    @Override
    protected void loadFromJson(@NotNull JsonObject jo) {
        JsonObject djo = jo.getAsJsonObject("data");

        if (djo != null) {
            for (Map.Entry<String, JsonElement> entry : djo.entrySet()) {
                if (entry.getValue().isJsonObject()) {
                    ServerUserDataImpl sudi = new ServerUserDataImpl(this::dirty);
                    sudi.loadFromJson(entry.getValue().getAsJsonObject());

                    try {
                        serverUserData.put(Long.parseLong(entry.getKey()), sudi);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
    }

    @Override
    protected void saveToJson(@NotNull JsonObject jo) {
        JsonObject djo = new JsonObject();

        serverUserData.forEach((id, sud) -> {
            JsonObject j = new JsonObject();
            sud.saveToJson(j);
            djo.add(String.valueOf(id), j);
        });

        jo.add("data", djo);
    }

    @Override
    protected int getVersion() {
        return ServerUserData.VERSION;
    }

    protected ServerUserData getUserData(long userId) {
        return serverUserData.computeIfAbsent(userId, id -> new ServerUserDataImpl(this::dirty));
    }

    @NotNull
    @Unmodifiable
    protected Map<Long, ServerUserData> getAllUserData() {
        return ImmutableMap.copyOf(serverUserData);
    }
}
