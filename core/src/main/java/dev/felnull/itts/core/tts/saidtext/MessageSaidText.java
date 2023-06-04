package dev.felnull.itts.core.tts.saidtext;

import dev.felnull.itts.core.ITTSRuntimeUse;
import dev.felnull.itts.core.util.DiscordUtils;
import dev.felnull.itts.core.voice.Voice;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.internal.entities.SystemMessage;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.RejectedExecutionException;

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

        return CompletableFuture.completedFuture(message.getContentDisplay());
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
