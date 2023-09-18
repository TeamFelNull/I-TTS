package dev.felnull.itts.core.util;

import dev.felnull.itts.core.ITTSRuntime;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Discordに関するユーティリティ
 *
 * @author MORIMORI0317
 */
public final class DiscordUtils {

    private DiscordUtils() {
    }

    /**
     * 非同期で名前を取得する。<br>
     * キャッシュにメンバーが存在しない場合は取得を行うため時間がかかる
     *
     * @param guild ギルド
     * @param user  ユーザー
     * @return 名前のCompletableFuture
     */
    @NotNull
    public static CompletableFuture<String> getNameAsync(@Nullable Guild guild, @NotNull User user) {
        Objects.requireNonNull(user);

        if (guild != null) {
            Member member = guild.getMember(user);

            if (member == null) {
                CacheRestAction<Member> memberCacheRestAction = guild.retrieveMember(user);

                return CompletableFuture.supplyAsync(() -> getName(memberCacheRestAction.complete()), ITTSRuntime.getInstance().getAsyncWorkerExecutor());
            } else {
                return CompletableFuture.completedFuture(getName(member));
            }

        }

        return CompletableFuture.completedFuture(user.getName());
    }


    /**
     * 名前もしくはニックネームを取得する
     * キャッシュに名前が存在しない場合はユーザの名前自体を取得する
     *
     * @param guild サーバー
     * @param user  ユーザ
     * @return 名前
     */
    @NotNull
    public static String getName(@Nullable Guild guild, @NotNull User user) {
        Objects.requireNonNull(user);

        if (guild != null) {
            Member member = guild.getMember(user);
            if (member != null) {
                return getName(member);
            }
        }

        return user.getName();
    }

    /**
     * 名前もしくはニックネームを取得する
     *
     * @param member メンバー
     * @return 名前
     */
    @NotNull
    public static String getName(@NotNull Member member) {
        Objects.requireNonNull(member);

        return Objects.requireNonNullElseGet(member.getNickname(), () -> member.getUser().getName());
    }

    /**
     * メンションをエスケープした名前もしくはニックネームを取得する
     *
     * @param member メンバー
     * @return 名前
     */
    @NotNull
    public static String getEscapedName(@NotNull Member member) {
        return escapeMention(getName(member));
    }


    /**
     * メンションをエスケープした名前を取得する
     *
     * @param guild サーバー
     * @param user  ユーザ
     * @return 名前
     */
    @NotNull
    public static String getEscapedName(@Nullable Guild guild, @NotNull User user) {
        return escapeMention(getName(guild, user));
    }

    /**
     * メンションをエスケープした名前もしくはニックネームを取得する
     * キャッシュに名前が存在しない場合はユーザの名前自体を取得する
     *
     * @param user ユーザ
     * @return 名前
     */
    @NotNull
    public static String getEscapedName(@NotNull User user) {
        Objects.requireNonNull(user);

        return escapeMention(user.getName());
    }

    /**
     * メンションをエスケープする
     *
     * @param txt エスケープ対象文字列
     * @return エスケープ済み文字列
     */
    @NotNull
    public static String escapeMention(@NotNull String txt) {
        Objects.requireNonNull(txt);

        txt = Message.MentionType.EVERYONE.getPattern().matcher(txt).replaceAll(n -> "everyone");
        txt = Message.MentionType.HERE.getPattern().matcher(txt).replaceAll(n -> "here");
        txt = Message.MentionType.USER.getPattern().matcher(txt).replaceAll(n -> n.group().substring(2, n.group().length() - 1));
        txt = Message.MentionType.ROLE.getPattern().matcher(txt).replaceAll(n -> n.group().substring(2, n.group().length() - 1));
        return txt;
    }
}
