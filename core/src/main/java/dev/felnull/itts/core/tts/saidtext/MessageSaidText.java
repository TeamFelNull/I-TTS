package dev.felnull.itts.core.tts.saidtext;

import dev.felnull.itts.core.ITTSRuntimeUse;
import dev.felnull.itts.core.util.DiscordUtils;
import dev.felnull.itts.core.voice.Voice;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReference;
import net.dv8tion.jda.api.entities.User;

import java.util.concurrent.CompletableFuture;

public record MessageSaidText(Message message, Voice voice) implements SaidText, ITTSRuntimeUse {
    private static final String REPLAY_MESSAGE = "%sに返信しました、%s";
    private static final String MY_MESSAGE = "自分";
    private static final String UNKNOWN_MESSAGE = "不明なメッセージ";

    @Override
    public CompletableFuture<String> getText() {
        String contentDisplay = message.getContentDisplay();
        MessageReference reference = message.getMessageReference();

        if (reference == null)
            return CompletableFuture.completedFuture(contentDisplay);

        return CompletableFuture.supplyAsync(() -> {
            String replayTarget;

            Message refMessage = reference.getMessage();

            if (refMessage != null) {
                User refUser = refMessage.getAuthor();

                if (refUser.getIdLong() == message.getAuthor().getIdLong()) {
                    replayTarget = MY_MESSAGE;
                } else {
                    replayTarget = DiscordUtils.getName(refMessage.getGuild(), refUser);
                }

            } else {
                replayTarget = UNKNOWN_MESSAGE;
            }

            return String.format(REPLAY_MESSAGE, replayTarget, contentDisplay);
        }, getAsyncExecutor());
    }

    @Override
    public CompletableFuture<Voice> getVoice() {
        return CompletableFuture.completedFuture(voice);
    }
}
