package dev.felnull.ttsvoice.util;

import dev.felnull.ttsvoice.Main;
import dev.felnull.ttsvoice.discord.BotLocation;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.regex.Pattern;

public class DiscordUtils {
    private static final Pattern codeBlockPattern = Pattern.compile("```(.|\n)*```");
    private static final String codeBlockSyoryaku = "コードブロック省略";

    public static String getChannelName(GuildChannel channel, Member member, String other) {
        return other;
        /*if (member.hasPermission(channel, Permission.VIEW_CHANNEL))
            return channel.getName();
        return other;*/
    }

    public static String toCodeBlockSyoryaku(String text) {
        return codeBlockPattern.matcher(text).replaceAll(codeBlockSyoryaku);
    }

    public static String createChannelMention(Channel channel) {
        return "<#" + channel.getId() + ">";
    }

    public static String toNoMention(String txt) {
        if (txt == null) return null;
        txt = Message.MentionType.EVERYONE.getPattern().matcher(txt).replaceAll(n -> "everyone");
        txt = Message.MentionType.HERE.getPattern().matcher(txt).replaceAll(n -> "here");
        txt = Message.MentionType.USER.getPattern().matcher(txt).replaceAll(n -> n.group().substring(2, n.group().length() - 1));
        txt = Message.MentionType.ROLE.getPattern().matcher(txt).replaceAll(n -> n.group().substring(2, n.group().length() - 1));
        return txt;
    }

    public static String getName(BotLocation botLocation, User user, long userId) {
        return toNoMention(getName_(botLocation, user, userId));
    }

    private static String getName_(BotLocation botLocation, User user, long userId) {
        var unn = Main.getSaveData().getUserNickName(userId);
        if (unn != null)
            return unn;

        if (user == null)
            user = botLocation.getJDA().getUserById(userId);

        if (user == null)
            user = botLocation.getJDA().retrieveUserById(userId).complete();

        if (user == null)
            return String.valueOf(userId);
        var member = botLocation.getGuild().getMember(user);

        if (member == null)
            member = botLocation.getGuild().retrieveMemberById(user.getIdLong()).complete();

        if (member == null)
            return user.getName();
        return getName_(member);
    }

    public static String getName(Member member) {
        return toNoMention(getName_(member));
    }

    private static String getName_(Member member) {
        var unn = Main.getSaveData().getUserNickName(member.getIdLong());
        if (unn != null)
            return unn;

        var nick = member.getNickname();
        if (nick == null)
            return member.getUser().getName();
        return nick;
    }

    public static boolean hasPermission(Member member) {
        boolean flg = member.getRoles().stream().anyMatch(n -> Main.getConfig().adminRoles().contains(n.getIdLong()));
        return flg || member.isOwner() || member.hasPermission(Permission.MANAGE_SERVER);
    }

    public static boolean hasNeedAdminPermission(Member member) {
        if (Main.getConfig().needAdminServers().contains(member.getGuild().getIdLong()))
            return hasPermission(member);
        return true;
    }

    public static String replaceMentionToText(BotLocation botLocation, String text) {
        for (Message.MentionType mentionType : Message.MentionType.values()) {
            text = replaceMentionToText(botLocation, mentionType, text);
        }
        return text;
    }

    public static String replaceMentionToText(BotLocation botLocation, Message.MentionType mention, String text) {
        return mention.getPattern().matcher(text).replaceAll(n -> {
            var p = n.group();
            if (mention == Message.MentionType.USER)
                return toUserMentionToText(botLocation, p);
            if (mention == Message.MentionType.CHANNEL)
                return toChannelMentionToText(botLocation.getGuild(), p);
            if (mention == Message.MentionType.ROLE)
                return toRoleMentionToText(botLocation.getGuild(), p);
            if (mention == Message.MentionType.EMOJI)
                return toEmojiMentionToText(botLocation.getGuild(), p);
            return p;
        });
    }

    private static String toUserMentionToText(BotLocation botLocation, String mentionText) {
        if (Message.MentionType.USER.getPattern().matcher(mentionText).matches()) {
            mentionText = mentionText.substring(2, mentionText.length() - 1);
            long id = Long.parseLong(mentionText);
            var nick = Main.getSaveData().getUserNickName(id);
            if (nick != null)
                return nick;
            var guild = botLocation.getGuild();
            var m = guild.getMemberById(id);
            if (m != null)
                return getName(m);
            var user = botLocation.getJDA().getUserById(id);
            if (user != null)
                return getName(botLocation, user, id);

            var user2 = botLocation.getJDA().retrieveUserById(id).complete();
            if (user2 != null)
                return getName(botLocation, user2, id);
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
        if (Message.MentionType.EMOJI.getPattern().matcher(mentionText).matches()) {
            mentionText = mentionText.substring(3);
            mentionText = mentionText.substring(0, mentionText.indexOf(":"));
            mentionText = mentionText.replaceAll("_", " ");
        }
        return mentionText;
    }

    public static boolean isNonAllowInm(long guildId) {
        return guildId == 930083398691733565L;
    }

    public static boolean isNonAllowCookie(long guildId) {
        return guildId == 930083398691733565L;
    }
}
