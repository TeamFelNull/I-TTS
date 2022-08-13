package dev.felnull.ttsvoice.tts.tracker;

import dev.felnull.ttsvoice.discord.BotLocation;
import dev.felnull.ttsvoice.tts.TTSManager;
import dev.felnull.ttsvoice.tts.TTSVoice;
import net.dv8tion.jda.api.entities.Message;

public class TextMessageTTSTracker extends BaseTTSTracker {
    private final BotLocation botLocation;
    private final String messageText;
    private final long channelId;
    private final long messageId;
    private final Runnable deposeRun;

    public TextMessageTTSTracker(BotLocation botLocation, String messageText, long channelId, long messageId, Runnable deposeRun) {
        this.botLocation = botLocation;
        this.messageText = messageText;
        this.channelId = channelId;
        this.messageId = messageId;
        this.deposeRun = deposeRun;
    }

    @Override
    public TTSTrackerInfo getUpdateVoice() {
        var channel = botLocation.getGuild().getTextChannelById(channelId);
        if (channel == null) return new TTSTrackerInfo(true, null);
        Message msg;
        try {
            msg = channel.retrieveMessageById(messageId).complete();
        } catch (Exception ex) {
            return new TTSTrackerInfo(true, null);
        }
        var nmsgt = msg.getContentRaw();
        if (!messageText.equals(nmsgt)) {
            var nmsg = TTSManager.getInstance().createSayChat(botLocation, msg.getAuthor().getIdLong(), nmsgt);
            return new TTSTrackerInfo(false, new TTSVoice(nmsg.getLeft(), nmsg.getRight()));
        }
        return new TTSTrackerInfo(false, null);
    }

    public void onUpdateMessage(String text, long userId) {
        if (text != null) {
            if (!messageText.equalsIgnoreCase(text)) {
                var nmsg = TTSManager.getInstance().createSayChat(botLocation, userId, text);
                onUpdateVoice(new TTSTrackerInfo(false, new TTSVoice(nmsg.getLeft(), nmsg.getRight())));
            }
        } else {
            onUpdateVoice(new TTSTrackerInfo(true, null));
        }
    }

    @Override
    public void depose() {
        deposeRun.run();
    }
}
