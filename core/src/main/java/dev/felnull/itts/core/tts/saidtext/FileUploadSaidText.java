package dev.felnull.itts.core.tts.saidtext;

import dev.felnull.itts.core.ITTSRuntimeUse;
import dev.felnull.itts.core.voice.Voice;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public record FileUploadSaidText(Voice voice, List<Message.Attachment> attachments) implements SaidText, ITTSRuntimeUse {

    @Override
    public CompletableFuture<String> getText() {
        return CompletableFuture.supplyAsync(() -> {
            StringBuilder sb = new StringBuilder();

            int count = 0;

            var fileTypes = attachments.stream()
                    .map(FileUploadSaidText::getFileType)
                    .collect(Collectors.groupingBy(it -> it));

            for (Map.Entry<FileType, List<FileType>> entry : fileTypes.entrySet()) {

                int ct = entry.getValue().size();
                if (ct >= 2)
                    sb.append(entry.getValue().size()).append("個の");

                sb.append(entry.getKey().getName());

                if (count < fileTypes.size() - 1) {
                    if (count == 0) {
                        sb.append("と");
                    } else {
                        sb.append(",");
                    }
                }

                count++;
            }

            sb.append("をアップロードしました");

            return sb.toString();
        }, getAsyncExecutor());
    }

    @Override
    public CompletableFuture<Voice> getVoice() {
        return CompletableFuture.completedFuture(voice);
    }

    private static FileType getFileType(Message.Attachment attachment) {
        if (attachment.isImage())
            return FileType.IMAGE;

        if (attachment.isVideo())
            return FileType.VIDEO;

        var ct = attachment.getContentType();
        if (ct != null && ct.startsWith("audio/"))
            return FileType.AUDIO;

        return FileType.FILE;
    }

    private static enum FileType {
        FILE("ファイル"),
        IMAGE("画像"),
        VIDEO("動画"),
        AUDIO("音声");
        private final String name;

        FileType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
