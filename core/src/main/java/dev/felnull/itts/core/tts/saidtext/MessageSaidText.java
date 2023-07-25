package dev.felnull.itts.core.tts.saidtext;

import dev.felnull.itts.core.ITTSRuntimeUse;
import dev.felnull.itts.core.util.DiscordUtils;
import dev.felnull.itts.core.voice.Voice;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public record MessageSaidText(Message message, Voice voice) implements SaidText, ITTSRuntimeUse {
    private static final String REPLAY_MESSAGE = "%sに返信しました、%s";
    private static final String MY_MESSAGE = "自分";
    private static final String UNKNOWN_MESSAGE = "不明なメッセージ";
    private static final String PINNED_MESSAGE = "%sのメッセージをチャンネルにピン留めしました";
    private static final String UNKNOWN_PINNED_MESSAGE = "メッセージをチャンネルにピン留めしました";

    @Override
    public CompletableFuture<String> getText() {
        MessageType messageType = message.getType();

        if (messageType == MessageType.INLINE_REPLY)
            return inlineReply();

        if (messageType == MessageType.CHANNEL_PINNED_ADD)
            return pined();

        Map<User, Member> members = new ConcurrentHashMap<>();

        Stream<CompletableFuture<Void>> memberLoad = message.getMentions().getUsers().stream()
                .map(user -> {

                    Guild guild = message.getGuild();

                    if (!guild.isMember(user)) {
                        return CompletableFuture.runAsync(() -> members.put(user, guild.retrieveMember(user).complete()), getAsyncExecutor());
                    }

                    members.put(user, guild.getMember(user));
                    return CompletableFuture.completedFuture(null);
                });


        return CompletableFuture.allOf(memberLoad.toArray(CompletableFuture[]::new))
                .thenApplyAsync(v -> getIkisugiContentDisplay(members, message), getAsyncExecutor());
    }

    /**
     * {@link Message#getContentDisplay()}のメンバー取得がキャッシュを利用しないためユーザIDを読み上げてしまうことの回避策
     *
     * @param members メンバーとユーザ名
     * @param message メッセージ
     * @return 文字列
     */
    private static String getIkisugiContentDisplay(Map<User, Member> members, Message message) {
        String ret = message.getContentRaw();
        for (User user : message.getMentions().getUsers()) {
            String name;
            name = members.get(user).getEffectiveName();
            ret = ret.replaceAll("<@!?" + Pattern.quote(user.getId()) + '>', '@' + Matcher.quoteReplacement(name));
        }
        for (CustomEmoji emoji : message.getMentions().getCustomEmojis()) {
            ret = ret.replace(emoji.getAsMention(), ":" + emoji.getName() + ":");
        }
        for (GuildChannel mentionedChannel : message.getMentions().getChannels()) {
            ret = ret.replace(mentionedChannel.getAsMention(), '#' + mentionedChannel.getName());
        }
        for (Role mentionedRole : message.getMentions().getRoles()) {
            ret = ret.replace(mentionedRole.getAsMention(), '@' + mentionedRole.getName());
        }
        return ret;
    }

    /**
     * ピン止めされた時のメッセージ
     *
     * @return 読み上げる文字列
     */
    private CompletableFuture<String> pined() {
        return CompletableFuture.supplyAsync(() -> {
            MessageReference reference = message.getMessageReference();

            String pinedTarget = null;

            if (reference != null) {
                Message pinMessage = reference.getMessage();

                if (pinMessage == null) {
                    try {
                        pinMessage = message.getChannel().retrieveMessageById(reference.getMessageIdLong()).complete();
                    } catch (RuntimeException ignored) {
                    }
                }

                if (pinMessage != null) {
                    User pinAuthor = pinMessage.getAuthor();

                    if (pinAuthor.getIdLong() == message.getAuthor().getIdLong()) {
                        pinedTarget = MY_MESSAGE;
                    } else {
                        pinedTarget = DiscordUtils.getName(pinMessage.getGuild(), pinAuthor);
                    }
                }
            }

            return pinedTarget != null ? String.format(PINNED_MESSAGE, pinedTarget) : UNKNOWN_PINNED_MESSAGE;
        }, getAsyncExecutor());
    }

    /**
     * 返信時のメッセージ
     *
     * @return 読み上げる文字列
     */
    private CompletableFuture<String> inlineReply() {
        return CompletableFuture.supplyAsync(() -> {
            MessageReference reference = message.getMessageReference();
            Message refMessage;

            String replayTarget;

            if (reference != null && (refMessage = reference.getMessage()) != null) {
                User refUser = refMessage.getAuthor();

                if (refUser.getIdLong() == message.getAuthor().getIdLong()) {
                    replayTarget = MY_MESSAGE;
                } else {
                    replayTarget = DiscordUtils.getName(refMessage.getGuild(), refUser);
                }

            } else {
                replayTarget = UNKNOWN_MESSAGE;
            }

            return String.format(REPLAY_MESSAGE, replayTarget, message.getContentDisplay());
        }, getAsyncExecutor());
    }

    @Override
    public CompletableFuture<Voice> getVoice() {
        return CompletableFuture.completedFuture(voice);
    }
}
