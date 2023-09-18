package dev.felnull.itts.core.tts.saidtext;

import dev.felnull.itts.core.ITTSRuntimeUse;
import dev.felnull.itts.core.voice.Voice;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * ファイル送信時の読み上げテキスト
 *
 * @param voice       音声タイプ
 * @param attachments メッセージのアタッチメント
 * @author MORIMORI0317
 */
public record FileUploadSaidText(Voice voice,
                                 List<Message.Attachment> attachments) implements SaidText, ITTSRuntimeUse {

    @Override
    public CompletableFuture<String> getText() {
        return CompletableFuture.supplyAsync(() -> {
            StringBuilder sb = new StringBuilder();

            int count = 0;

            Map<FileType, List<FileType>> fileTypes = attachments.stream()
                    .map(FileUploadSaidText::getFileType)
                    .collect(Collectors.groupingBy(it -> it));

            for (Map.Entry<FileType, List<FileType>> entry : fileTypes.entrySet()) {

                int ct = entry.getValue().size();

                if (ct >= 2) {
                    sb.append(entry.getValue().size()).append("個の");
                }

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
        if (attachment.isImage()) {
            return FileType.IMAGE;
        }

        if (attachment.isVideo()) {
            return FileType.VIDEO;
        }

        String ct = attachment.getContentType();
        if (ct != null && ct.startsWith("audio/")) {
            return FileType.AUDIO;
        }
        return FileType.FILE;
    }

    /**
     * ファイルの種類
     *
     * @author MORIMORI0317
     */
    private enum FileType {
        /**
         * 未分類のファイル
         */
        FILE("ファイル"),

        /**
         * 画像ファイル
         */
        IMAGE("画像"),

        /**
         * 動画ファイル
         */
        VIDEO("動画"),

        /**
         * 音声ファイル
         */
        AUDIO("音声");

        /**
         * 名前
         */
        private final String name;

        FileType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
