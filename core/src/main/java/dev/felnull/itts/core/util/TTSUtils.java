package dev.felnull.itts.core.util;

import dev.felnull.itts.core.ITTSRuntime;
import dev.felnull.itts.core.savedata.ServerUserData;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel;
import org.jetbrains.annotations.NotNull;

public final class TTSUtils {
    @NotNull
    public static String getTTSName(@NotNull Member member) {
        User user = member.getUser();
        ServerUserData sud = ITTSRuntime.getInstance().getSaveDataManager().getServerUserData(member.getGuild().getIdLong(), user.getIdLong());
        String nick = sud.getNickName();

        if (nick != null)
            return nick;

        return DiscordUtils.getName(user);
    }

    @NotNull
    public static String getTTSChannelName(@NotNull StandardGuildChannel channel) {
        if (channel.getPermissionOverrides().isEmpty())
            return channel.getName();

        return "別のチャンネル";
    }
}
