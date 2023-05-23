package dev.felnull.itts.core.util;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DiscordUtils {

    @NotNull
    public static String getName(Member member) {
        String nick = member.getNickname();
        if (nick != null)
            return escapeMention(nick);

        return getName(member.getUser());
    }

    @NotNull
    public static String getName(@Nullable Guild guild, User user) {

        if (guild != null) {
            Member member = guild.getMember(user);
            if (member != null)
                return getName(member);
        }

        return getName(user);
    }

    @NotNull
    public static String getName(User user) {
        return escapeMention(user.getName());
    }

    public static String escapeMention(String txt) {
        if (txt == null) return null;
        txt = Message.MentionType.EVERYONE.getPattern().matcher(txt).replaceAll(n -> "everyone");
        txt = Message.MentionType.HERE.getPattern().matcher(txt).replaceAll(n -> "here");
        txt = Message.MentionType.USER.getPattern().matcher(txt).replaceAll(n -> n.group().substring(2, n.group().length() - 1));
        txt = Message.MentionType.ROLE.getPattern().matcher(txt).replaceAll(n -> n.group().substring(2, n.group().length() - 1));
        return txt;
    }
}
