package dev.felnull.itts.core.util;

import dev.felnull.itts.core.ITTSRuntime;
import dev.felnull.itts.core.savedata.ServerData;
import dev.felnull.itts.core.savedata.ServerUserData;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class TTSUtils {
    @NotNull
    public static String getTTSName(@NotNull Member member) {
        User user = member.getUser();
        ServerUserData sud = ITTSRuntime.getInstance().getSaveDataManager().getServerUserData(member.getGuild().getIdLong(), user.getIdLong());
        String nick = sud.getNickName();

        String ret = Objects.requireNonNullElseGet(nick, () -> DiscordUtils.getName(member));

        return roundText(member.getGuild().getIdLong(), ret, true);
    }

    public static String roundText(long guildId, String text, boolean name) {
        ServerData sud = ITTSRuntime.getInstance().getSaveDataManager().getServerData(guildId);
        int max = name ? sud.getNameReadLimit() : sud.getReadLimit();

        if (text.length() <= max)
            return text;

        int r = text.length() - max;

        return text.substring(0, max) + "以下" + r + "文字を所略";
    }

    @NotNull
    public static String getTTSChannelName(@NotNull StandardGuildChannel channel) {
        if (channel.getPermissionOverrides().isEmpty())
            return channel.getName();

        return "別のチャンネル";
    }
}
