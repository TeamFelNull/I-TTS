package dev.felnull.ttsvoice.core.util;

import net.dv8tion.jda.api.entities.channel.Channel;
import org.jetbrains.annotations.NotNull;

public class DiscordUtils {
    /**
     * チャンネルのメンションを作成する
     *
     * @param channel チャンネル
     * @return チャンネルメンションテキスト
     */
    @NotNull
    public static String createChannelMention(@NotNull Channel channel) {
        return "<#" + channel.getId() + ">";
    }
}
