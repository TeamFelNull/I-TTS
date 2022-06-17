package dev.felnull.ttsvoice.util;

import dev.felnull.ttsvoice.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

public class DiscordUtils {
    public static String createChannelMention(Channel channel) {
        return "<#" + channel.getId() + ">";
    }

    public static String getName(Guild guild, User user, long userId) {
        var unn = Main.SAVE_DATA.getUserNickName(userId);
        if (unn != null)
            return unn;

        if (user == null)
            user = Main.JDA.getUserById(userId);

        if (user == null)
            user = Main.JDA.retrieveUserById(userId).complete();

        if (user == null)
            return String.valueOf(userId);
        var member = guild.getMember(user);
        if (member == null)
            return user.getName();
        return getName(member);
    }

    public static String getName(Member member) {
        var unn = Main.SAVE_DATA.getUserNickName(member.getIdLong());
        if (unn != null)
            return unn;

        var nick = member.getNickname();
        if (nick == null)
            return member.getUser().getName();
        return nick;
    }

    public static boolean hasPermission(Member member) {
        boolean flg = member.getRoles().stream().anyMatch(n -> Main.CONFIG.adminRoles().contains(n.getIdLong()));
        return flg || member.isOwner() || member.hasPermission(Permission.MANAGE_SERVER);
    }

    public static boolean hasNeedAdminPermission(Member member) {
        if (Main.CONFIG.needAdminServers().contains(member.getGuild().getIdLong()))
            return hasPermission(member);
        return true;
    }

    public static String replaceMentionToText(Guild guild, String text) {
        for (Message.MentionType mentionType : Message.MentionType.values()) {
            text = replaceMentionToText(guild, mentionType, text);
        }
        return text;
    }

    public static String replaceMentionToText(Guild guild, Message.MentionType mention, String text) {
        return mention.getPattern().matcher(text).replaceAll(n -> {
            var p = n.group();
            if (mention == Message.MentionType.USER)
                return toUserMentionToText(guild, p);
            if (mention == Message.MentionType.CHANNEL)
                return toChannelMentionToText(guild, p);
            if (mention == Message.MentionType.ROLE)
                return toRoleMentionToText(guild, p);
            if (mention == Message.MentionType.EMOTE)
                return toEmojiMentionToText(guild, p);
            return p;
        });
    }

    private static String toUserMentionToText(Guild guild, String mentionText) {
        if (Message.MentionType.USER.getPattern().matcher(mentionText).matches()) {
            mentionText = mentionText.substring(2, mentionText.length() - 1);
            long id = Long.parseLong(mentionText);
            var nick = Main.SAVE_DATA.getUserNickName(id);
            if (nick != null)
                return nick;

            var m = guild.getMemberById(id);
            if (m != null)
                return getName(m);
            var user = Main.JDA.getUserById(id);
            if (user != null)
                return getName(guild, user, id);

            var user2 = Main.JDA.retrieveUserById(id).complete();
            if (user2 != null)
                return getName(guild, user2, id);
        }
        return mentionText;
    }

    private static String toChannelMentionToText(Guild guild, String mentionText) {
        if (Message.MentionType.CHANNEL.getPattern().matcher(mentionText).matches()) {
            mentionText = mentionText.substring(2, mentionText.length() - 1);
            var m = guild.getGuildChannelById(mentionText);
            if (m != null)
                return m.getName();
        }
        return mentionText;
    }

    private static String toRoleMentionToText(Guild guild, String mentionText) {
        if (Message.MentionType.ROLE.getPattern().matcher(mentionText).matches()) {
            mentionText = mentionText.substring(3, mentionText.length() - 1);
            var m = guild.getRoleById(mentionText);
            if (m != null)
                return m.getName();
        }
        return mentionText;
    }

    private static String toEmojiMentionToText(Guild guild, String mentionText) {
        if (Message.MentionType.EMOTE.getPattern().matcher(mentionText).matches()) {
            mentionText = mentionText.substring(3);
            mentionText = mentionText.substring(0, mentionText.indexOf(":"));
            mentionText = mentionText.replaceAll("_", " ");
        }
        return mentionText;
    }

    public static boolean isNonAllowInm(long guildId) {
        return guildId == 930083398691733565L;
    }
}
