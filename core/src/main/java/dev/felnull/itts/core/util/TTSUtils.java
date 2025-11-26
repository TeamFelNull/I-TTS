package dev.felnull.itts.core.util;

import dev.felnull.itts.core.savedata.SaveDataManager;
import dev.felnull.itts.core.savedata.legacy.LegacySaveDataLayer;
import dev.felnull.itts.core.savedata.legacy.LegacyServerData;
import dev.felnull.itts.core.savedata.legacy.LegacyServerUserData;
import dev.felnull.itts.core.voice.Voice;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * TTS関係のユーティリティ
 *
 * @author MORIMORI0317
 */
public final class TTSUtils {
    private TTSUtils() {
    }

    /**
     * 読み上げられる名前を取得
     *
     * @param voice 声
     * @param guild サーバー
     * @param user  ユーザ
     * @return 名前
     */
    public static String getTTSName(@NotNull Voice voice, @NotNull Guild guild, @NotNull User user) {
        Objects.requireNonNull(voice);
        Objects.requireNonNull(guild);
        Objects.requireNonNull(user);

        Member member = guild.getMember(user);
        if (member != null) {
            return getTTSName(voice, member);
        }

        LegacySaveDataLayer legacySaveDataLayer = SaveDataManager.getInstance().getLegacySaveDataLayer();
        LegacyServerUserData sud = legacySaveDataLayer.getServerUserData(guild.getIdLong(), user.getIdLong());
        String nick = sud.getNickName();

        String ret = Objects.requireNonNullElseGet(nick, () -> DiscordUtils.getName(guild, user));
        return roundText(voice, guild.getIdLong(), ret, true);
    }


    /**
     * 読み上げられる名前を取得
     *
     * @param voice  声
     * @param member メンバー
     * @return 名前
     */
    @NotNull
    public static String getTTSName(@NotNull Voice voice, @NotNull Member member) {
        Objects.requireNonNull(voice);
        Objects.requireNonNull(member);

        User user = member.getUser();
        LegacySaveDataLayer legacySaveDataLayer = SaveDataManager.getInstance().getLegacySaveDataLayer();
        LegacyServerUserData sud = legacySaveDataLayer.getServerUserData(member.getGuild().getIdLong(), user.getIdLong());
        String nick = sud.getNickName();

        String ret = Objects.requireNonNullElseGet(nick, member::getEffectiveName);

        return roundText(voice, member.getGuild().getIdLong(), ret, true);
    }

    /**
     * 対象のテキストを読み上げるテキストに変換する<br/>
     * "以下省略"などの処理を行う
     *
     * @param voice   音声タイプ
     * @param guildId サーバーID
     * @param text    テキスト
     * @param name    名前かどうか
     * @return 読み上げてるテキスト
     */
    public static String roundText(Voice voice, long guildId, String text, boolean name) {
        LegacySaveDataLayer legacySaveDataLayer = SaveDataManager.getInstance().getLegacySaveDataLayer();
        LegacyServerData sud = legacySaveDataLayer.getServerData(guildId);
        int max = name ? sud.getNameReadLimit() : Math.min(sud.getReadLimit(), voice.getReadLimit());

        if (text.length() <= max) {
            return text;
        }

        String st = text.substring(0, max);

        if (name) {
            return st + "以下略";
        } else {
            int r = text.length() - max;
            return st + "以下" + r + "文字を省略";
        }
    }

    /**
     * 読み上げるチャンネル名を取得
     *
     * @param channel チャンネル
     * @return チャンネルの読み上げテキスト
     */
    @NotNull
    public static String getTTSChannelName(@NotNull StandardGuildChannel channel) {
        if (channel.getPermissionOverrides().isEmpty()) {
            return channel.getName();
        }

        return "別のチャンネル";
    }

    /**
     * 読み上げを聞くことが可能か確認
     *
     * @param voiceState 音声ステート
     * @return 結果
     */
    public static boolean canListen(GuildVoiceState voiceState) {
        User user = voiceState.getMember().getUser();
        if (user.isSystem() || user.isBot()) {
            return false;
        }

        return !voiceState.isDeafened();
    }
}
