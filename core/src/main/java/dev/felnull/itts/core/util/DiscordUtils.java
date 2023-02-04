package dev.felnull.itts.core.util;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import org.jetbrains.annotations.NotNull;

public final class DiscordUtils {
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

    @NotNull
    public static String getName(User user) {
        return mentionEscape(user.getName());
    }

    public static String mentionEscape(String txt) {
        if (txt == null) return null;
        txt = Message.MentionType.EVERYONE.getPattern().matcher(txt).replaceAll(n -> "everyone");
        txt = Message.MentionType.HERE.getPattern().matcher(txt).replaceAll(n -> "here");
        txt = Message.MentionType.USER.getPattern().matcher(txt).replaceAll(n -> n.group().substring(2, n.group().length() - 1));
        txt = Message.MentionType.ROLE.getPattern().matcher(txt).replaceAll(n -> n.group().substring(2, n.group().length() - 1));
        return txt;
    }
}
