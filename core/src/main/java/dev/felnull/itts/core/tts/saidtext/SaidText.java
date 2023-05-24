package dev.felnull.itts.core.tts.saidtext;

import dev.felnull.itts.core.tts.VCEventType;
import dev.felnull.itts.core.voice.Voice;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface SaidText {
    static SaidText literal(@NotNull Voice voice, @NotNull String text) {
        return new LiteralSaidText(voice, text);
    }

    static SaidText vcEvent(@NotNull Voice voice, @NotNull VCEventType eventType, @NotNull Member member, @Nullable AudioChannelUnion join,
                            @Nullable AudioChannelUnion left) {
        return new VCEventSaidText(voice, eventType, member, join, left);
    }

    static SaidText fileUpload(@NotNull Voice voice, @NotNull List<Message.Attachment> attachments) {
        return new FileUploadSaidText(voice, attachments);
    }

    static SaidText message(@NotNull Voice voice, @NotNull Message message) {
        return new MessageSaidText(message, voice);
    }

    CompletableFuture<String> getText();

    CompletableFuture<Voice> getVoice();
}
