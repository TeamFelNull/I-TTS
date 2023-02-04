package dev.felnull.itts.core.util;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import org.jetbrains.annotations.NotNull;

public final class TTSUtils {

    @NotNull
    public static String getTTSName(@NotNull User user) {
        return DiscordUtils.getName(user);
    }

    @NotNull
    public static String getTTSChannelName(@NotNull Channel channel) {
        return null;
    }
}
