package dev.felnull.itts.core.tts;

import dev.felnull.itts.core.ITTSRuntimeUse;
import dev.felnull.itts.core.savedata.SaveDataManager;
import dev.felnull.itts.core.savedata.legacy.LegacySaveDataLayer;
import dev.felnull.itts.core.savedata.repository.BotStateData;
import dev.felnull.itts.core.savedata.repository.DataRepository;
import dev.felnull.itts.core.tts.saidtext.FileUploadSaidText;
import dev.felnull.itts.core.tts.saidtext.MessageSaidText;
import dev.felnull.itts.core.tts.saidtext.SaidText;
import dev.felnull.itts.core.tts.saidtext.VCEventSaidText;
import dev.felnull.itts.core.util.TTSUtils;
import dev.felnull.itts.core.voice.Voice;
import dev.felnull.itts.core.voice.VoiceType;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * TTS管理
 *
 * @author MORIMORI0317
 */
public class TTSManager implements ITTSRuntimeUse {

    /**
     * サーバーごとのTTSインスタンス
     */
    private final Map<Long, TTSInstance> instances = new ConcurrentHashMap<>();

    public int getTTSCount() {
        return instances.size();
    }

    /**
     * 利用者数を取得
     *
     * @return 利用者数
     */
    public int getUserCount() {
        List<Guild> guilds = getBot().getJDA().getGuilds();

        List<Long> ttsChannels = instances.values().stream()
                .map(TTSInstance::getAudioChannel)
                .toList();

        long users = guilds.stream()
                .flatMap(guild -> guild.getVoiceStates().stream())
                .filter(voiceState -> voiceState.getChannel() != null)
                .filter(voiceState -> {
                    AudioChannelUnion channelUnion = voiceState.getChannel();
                    if (channelUnion != null) {
                        return ttsChannels.contains(channelUnion.getIdLong());
                    }
                    return false;
                })
                .filter(TTSUtils::canListen)
                // .map(GuildVoiceState::getMember)
                // .distinct()
                .count();

        return (int) users;
    }

    /**
     * 読み上げチャンネルを変更
     *
     * @param guild       サーバーID
     * @param textChannel テキストチャンネル
     */
    public void setReadAroundChannel(@NotNull Guild guild, @NotNull MessageChannel textChannel) {
        long guildId = guild.getIdLong();

        DataRepository dataRepository = SaveDataManager.getInstance().getRepository();
        BotStateData botStateData = dataRepository.getBotStateData(guildId, getBot().getBotId());
        botStateData.setReadAroundTextChannel(textChannel.getIdLong());
    }

    /**
     * オーディオチャンネルに接続
     *
     * @param guild        サーバー
     * @param audioChannel 接続先オーディオチャンネル
     */
    public void connect(@NotNull Guild guild, @NotNull AudioChannel audioChannel) {
        long guildId = guild.getIdLong();
        long channelId = audioChannel.getIdLong();

        TTSInstance pre = getTTSInstance(guildId);

        if (pre != null) {
            if (pre.getAudioChannel() == channelId) {
                return;
            }
            disconnect(guild);
        }

        DataRepository dataRepository = SaveDataManager.getInstance().getRepository();
        BotStateData botStateData = dataRepository.getBotStateData(guildId, getBot().getBotId());
        Long readTextChannel = botStateData.getReadAroundTextChannel();
        boolean overwriteAloud = dataRepository.getServerData(guildId).isOverwriteAloud();

        if (readTextChannel != null) {
            instances.put(guildId, new TTSInstance(guild, channelId, readTextChannel, overwriteAloud));
        }

        botStateData.setSpeakAudioChannel(channelId);
    }

    /**
     * オーディオチャンネルから切断
     *
     * @param guild サーバー
     */
    public void disconnect(@NotNull Guild guild) {
        long guildId = guild.getIdLong();

        TTSInstance instance = getTTSInstance(guildId);
        if (instance == null) {
            return;
        }

        instance.dispose();
        instances.remove(guildId);

        DataRepository dataRepository = SaveDataManager.getInstance().getRepository();
        BotStateData botStateData = dataRepository.getBotStateData(guildId, getBot().getBotId());
        botStateData.setSpeakAudioChannel(null);
    }

    /**
     * オーディオチャンネルに再接続
     *
     * @param guild サーバー
     */
    public void reload(@NotNull Guild guild) {
        long guildId = guild.getIdLong();

        TTSInstance instance = getTTSInstance(guildId);
        if (instance == null) {
            return;
        }

        disconnect(guild);

        AudioChannel rc = guild.getChannelById(AudioChannel.class, instance.getAudioChannel());
        if (rc != null) {
            connect(guild, rc);
        }
    }

    /**
     * TTSインスタンスを取得
     *
     * @param guildId サーバーID
     * @return TTSインスタンス
     */
    @Nullable
    public TTSInstance getTTSInstance(long guildId) {
        return instances.get(guildId);
    }

    /**
     * 読み上げる
     *
     * @param guild          サーバー
     * @param messageChannel メッセージチャンネル
     * @param member         メンバー
     * @param message        メッセージ
     */
    public void sayChat(@NotNull Guild guild, @NotNull MessageChannel messageChannel, @Nullable Member member, @NotNull Message message) {
        if (message.getContentRaw().isEmpty()) {
            return;
        }

        LegacySaveDataLayer legacySaveDataLayer = SaveDataManager.getInstance().getLegacySaveDataLayer();

        String ignoreRegex = legacySaveDataLayer.getServerData(guild.getIdLong()).getIgnoreRegex();
        if (ignoreRegex != null) {
            Pattern ignorePattern = Pattern.compile(ignoreRegex);
            if (ignorePattern.matcher(message.getContentDisplay()).matches()) {
                return;
            }
        }

        sayGuildMemberText(guild, messageChannel, member, voice -> new MessageSaidText(message, voice));
    }

    /**
     * ファイル送信を読み上げる
     *
     * @param guild          サーバー
     * @param messageChannel メッセージチャンネル
     * @param member         メンバー
     * @param attachments    アタッチメント
     */
    public void sayUploadFile(@NotNull Guild guild, @NotNull MessageChannel messageChannel, @Nullable Member member, @NotNull List<Message.Attachment> attachments) {
        if (attachments.isEmpty()) {
            return;
        }

        sayGuildMemberText(guild, messageChannel, member, voice -> new FileUploadSaidText(voice, attachments));
    }

    /**
     * サーバーメンバーのテキスト読み上げを行う
     *
     * @param guild           サーバー
     * @param messageChannel  メッセージのチャンネル
     * @param member          メンバー
     * @param saidTextFactory 読み上げテキストの生成ファンクション
     */
    public void sayGuildMemberText(@NotNull Guild guild, @NotNull MessageChannel messageChannel,
                                   @Nullable Member member, @NotNull Function<Voice, SaidText> saidTextFactory) {
        LegacySaveDataLayer legacySaveDataLayer = SaveDataManager.getInstance().getLegacySaveDataLayer();

        if (!canSpeak(guild)) {
            return;
        }

        long guildId = guild.getIdLong();
        long textChannelId = messageChannel.getIdLong();

        if (member == null) {
            return;
        }

        User user = member.getUser();
        if (user.isBot() || user.isSystem()) {
            return;
        }

        long userId = user.getIdLong();

        if (legacySaveDataLayer.getServerUserData(guildId, userId).isDeny()) {
            return;
        }

        TTSInstance ti = getTTSInstance(guildId);
        if (ti == null || ti.getTextChannel() != textChannelId) {
            return;
        }

        if (legacySaveDataLayer.getServerData(guildId).isNeedJoin()) {
            GuildVoiceState vs = member.getVoiceState();
            if (vs == null) {
                return;
            }

            AudioChannelUnion vc = vs.getChannel();
            if (vc == null || vc.getIdLong() != ti.getAudioChannel()) {
                return;
            }
        }

        VoiceType vt = getVoiceManager().getVoiceType(guildId, userId);
        if (vt == null) {
            return;
        }

        ti.sayText(saidTextFactory.apply(vt.createVoice(guildId, userId)));
    }

    /**
     * テキストの読み上げを行う
     *
     * @param guild    サーバー
     * @param saidText 読み上げテキスト
     */
    public void sayText(@NotNull Guild guild, @NotNull SaidText saidText) {
        if (!canSpeak(guild)) {
            return;
        }

        long guildId = guild.getIdLong();

        TTSInstance ti = getTTSInstance(guildId);
        if (ti == null) {
            return;
        }

        ti.sayText(saidText);
    }

    /**
     * 読み上げ可能かどうか調べる
     *
     * @param guild サーバー
     * @return 読み上げ可能かどうか
     */
    public boolean canSpeak(@NotNull Guild guild) {
        long guildId = guild.getIdLong();

        TTSInstance ti = getTTSInstance(guildId);
        if (ti == null) {
            return false;
        }

        AudioChannel audioChannel = guild.getChannelById(AudioChannel.class, ti.getAudioChannel());
        if (audioChannel == null) {
            return false;
        }

        return guild.getVoiceStates().stream().
                filter(it -> it.getChannel() != null && it.getChannel().getIdLong() == audioChannel.getIdLong())
                .anyMatch(TTSUtils::canListen);
    }

    /**
     * VCイベントの処理
     *
     * @param guild  サーバー
     * @param member メンバー
     * @param join   参加チャンネル
     * @param left   退出チャンネル
     */
    public void onVCEvent(@NotNull Guild guild, @NotNull Member member, @Nullable AudioChannelUnion join, @Nullable AudioChannelUnion left) {
        long guildId = guild.getIdLong();
        User user = member.getUser();
        long userId = user.getIdLong();

        TTSInstance ti = getTTSInstance(guildId);
        if (ti == null || !((join != null && ti.getAudioChannel() == join.getIdLong()) || (left != null && ti.getAudioChannel() == left.getIdLong()))) {
            return;
        }

        LegacySaveDataLayer legacySaveDataLayer = SaveDataManager.getInstance().getLegacySaveDataLayer();
        if (!legacySaveDataLayer.getServerData(guildId).isNotifyMove()) {
            return;
        }

        VoiceType vt = getVoiceManager().getVoiceType(guildId, userId);
        if (vt == null) {
            return;
        }

        if (join != null && join.getIdLong() == ti.getAudioChannel()) {
            GuildVoiceState vcs = member.getVoiceState();
            if (vcs != null && vcs.isGuildMuted()) {
                return;
            }
        }

        VCEventType vce = null;

        if (join != null && left == null) {
            vce = VCEventType.JOIN;
        } else if (join == null) {
            vce = VCEventType.LEAVE;
        } else if (join.getIdLong() == ti.getAudioChannel() && left.getIdLong() != ti.getAudioChannel()) {
            vce = VCEventType.MOVE_FROM;
        } else if (join.getIdLong() != ti.getAudioChannel() && left.getIdLong() == ti.getAudioChannel()) {
            vce = VCEventType.MOVE_TO;
        }

        if (canSpeak(guild)) {
            sayVCEvent(vce, ti, vt.createVoice(guildId, userId), member, join, left);
        }
    }

    private void sayVCEvent(VCEventType vcEventType, TTSInstance ttsInstance, Voice voice, Member member, AudioChannelUnion join, AudioChannelUnion left) {
        if (vcEventType == null) {
            return;
        }

        ttsInstance.sayText(new VCEventSaidText(voice, vcEventType, member, join, left));
    }
}
