package dev.felnull.ttsvoice.util;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;

public class DiscordUtil {
    public static String createChannelMention(Channel channel) {
        return "<#" + channel.getId() + ">";
    }

    public static boolean canEdit(List<Role> role) {
        return role.stream().anyMatch(DiscordUtil::canEdit);
    }

    public static boolean canEdit(Role role) {
        return role.getPermissions().contains(Permission.ADMINISTRATOR);
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
            var m = guild.getMemberById(mentionText);
            if (m != null)
                return m.getEffectiveName();
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
}
