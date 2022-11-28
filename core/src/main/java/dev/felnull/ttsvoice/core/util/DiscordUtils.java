package dev.felnull.ttsvoice.core.util;

import net.dv8tion.jda.api.entities.channel.Channel;

public class DiscordUtils {
    /**
     * チャンネルのメンションを作成する
     *
     * @param channel チャンネル
     * @return チャンネルメンションテキスト
     */
    public static String createChannelMention(Channel channel) {
        return "<#" + channel.getId() + ">";
    }
}
