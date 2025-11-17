package dev.felnull.itts.core.discord;

import dev.felnull.itts.core.ITTSRuntime;
import dev.felnull.itts.core.ImmortalityTimer;
import dev.felnull.itts.core.savedata.SaveDataManager;
import dev.felnull.itts.core.savedata.repository.BotStateData;
import dev.felnull.itts.core.savedata.repository.DataRepository;
import dev.felnull.itts.core.tts.TTSChannelPair;
import dev.felnull.itts.core.tts.TTSInstance;
import dev.felnull.itts.core.tts.TTSManager;
import dev.felnull.itts.core.tts.saidtext.StartupSaidText;
import dev.felnull.itts.core.voice.VoiceManager;
import dev.felnull.itts.core.voice.VoiceType;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 接続制御を行うクラス<br/>
 * 今のところ起動後の再接続と、自動切断、自動切断後の再接続の処理のみ
 *
 * @author MORIMORI0317
 */
public class ConnectControl {

    /**
     * ユーザーがいなくなってから自動切断するまでの待機時間
     */
    private static final Duration DISCONNECT_WAIT_DURATION = Duration.of(10, ChronoUnit.SECONDS);

    /**
     * Discordイベントアダプタ
     */
    private final DiscordEventAdaptor adaptor = new DiscordEventAdaptor();

    /**
     * 自動切断の待機タイマーのマップ
     */
    private final Map<Long, AutoDisconnecter> autoDisconnecters = new Long2ObjectOpenHashMap<>();

    protected DiscordEventAdaptor getAdaptor() {
        return adaptor;
    }


    private void startAutoDisconnecter(long guildId) {
        AutoDisconnecter disconnecter = null;

        synchronized (autoDisconnecters) {
            if (!autoDisconnecters.containsKey(guildId)) {
                disconnecter = new AutoDisconnecter(guildId);
                autoDisconnecters.put(guildId, disconnecter);
            }
        }

        if (disconnecter != null) {
            disconnecter.start();
        }
    }

    private void stopAutoDisconnecter(long guildId) {
        AutoDisconnecter disconnecter;
        synchronized (autoDisconnecters) {
            disconnecter = autoDisconnecters.remove(guildId);
        }

        if (disconnecter != null) {
            disconnecter.cancel();
        }
    }

    /**
     * 起動後の再接続
     */
    private void startUpReconnect(long guildId, BotStateData data) {
        JDA jda = ITTSRuntime.getInstance().getBot().getJDA();
        Guild guild = jda.getGuildById(guildId);

        if (guild == null) {
            return;
        }

        long audioCh = -1;
        long textCh = -1;

        DataRepository repo = SaveDataManager.getInstance().getRepository();
        AutoDisconnectMode autoDisMode = repo.getServerData(guildId).getAutoDisconnectMode();
        TTSChannelPair connectedChannelPair = data.getConnectedChannelPair();

        if (connectedChannelPair != null) {
            // 最終起動時に接続している場合
            boolean conFlg = false;

            if (autoDisMode.isOn()) {
                // 自動切断オンの場合
                AudioChannel audioChannel = guild.getVoiceChannelById(connectedChannelPair.speakAudioChannel());

                if (audioChannel != null) {
                    if (!isNoUser(audioChannel)) {
                        // ユーザーがいれば再接続
                        conFlg = true;
                    } else if (autoDisMode.isReconnect()) {
                        // 起動時の再接続はしないが、人が来たら再接続
                        data.setReconnectChannelPair(connectedChannelPair);
                    }
                }

            } else {
                // 自動切断オフの場合
                conFlg = true;
            }

            if (conFlg) {
                audioCh = connectedChannelPair.speakAudioChannel();
                textCh = connectedChannelPair.readTextChannel();
            }

        } else if (repo.getServerData(guildId).getAutoDisconnectMode().isReconnect()) {
            // 最終起動時に接続していなかったが、再接続予定で人がいれば接続
            TTSChannelPair reconnectChannel = repo.getBotStateData(guildId, ITTSRuntime.getInstance().getBot().getBotId()).getReconnectChannelPair();
            if (reconnectChannel != null) {
                AudioChannel audioChannel = guild.getVoiceChannelById(reconnectChannel.speakAudioChannel());

                if (audioChannel != null && !isNoUser(audioChannel)
                        && reconnectChannel.speakAudioChannel() != -1 && reconnectChannel.readTextChannel() != -1) {
                    audioCh = reconnectChannel.speakAudioChannel();
                    textCh = reconnectChannel.readTextChannel();
                }
            }
        }

        if (audioCh < 0 || textCh < 0) {
            return;
        }

        TTSManager ttsManager = ITTSRuntime.getInstance().getTTSManager();
        Logger logger = ITTSRuntime.getInstance().getLogger();
        VoiceManager voiceManager = ITTSRuntime.getInstance().getVoiceManager();
        long selfId = jda.getSelfUser().getIdLong();

        try {
            AudioChannel audioChannel = guild.getChannelById(AudioChannel.class, audioCh);

            // オーディオチャンネルが存在しない場合
            if (audioChannel == null) {
                data.setConnectedChannelPair(null);
                logger.info("Failed to reconnect (Audio channel does not exist): {}", guild.getName());
                return;
            }

            TextChannel chatChannel = guild.getTextChannelById(textCh);

            // テキストチャンネルが存在しない場合
            if (chatChannel == null) {
                data.setConnectedChannelPair(null);
                logger.info("Failed to reconnect (Message channel does not exist): {}", guild.getName());
                return;
            }

            // 再接続
            ttsManager.setReadAroundChannel(guild, chatChannel);

            try {
                guild.getAudioManager().openAudioConnection(audioChannel);
            } catch (InsufficientPermissionException ex) {
                data.setConnectedChannelPair(null);
                logger.info("Failed to reconnect (No permission): {}", guild.getName());
                return;
            }

            ttsManager.connect(guild, audioChannel);

            // 可能の場合は、再起動したことを読み上げる
            TTSInstance ti = ttsManager.getTTSInstance(guildId);
            VoiceType vt = voiceManager.getVoiceType(guildId, selfId);

            if (ti != null && vt != null && ttsManager.canSpeak(guild)) {
                ti.sayText(new StartupSaidText(vt.createVoice(guildId, selfId)));
            }

            logger.info("Reconnected: {}", guild.getName());
        } catch (Exception ex) {
            logger.error("Failed to reconnect: {}", guild.getName(), ex);
        }
    }

    /**
     * ユーザーがチャンネルに参加したときの再接続
     */
    private void joinReconnect(TTSChannelPair ttsChannel, Guild guild, AudioChannelUnion channelUnion, AudioManager audioManager) {
        TTSManager ttsManager = ITTSRuntime.getInstance().getTTSManager();
        TextChannel channel = guild.getTextChannelById(ttsChannel.readTextChannel());

        if (channel == null) {
            // 再接続を行う予定の読み上げテキストチャンネルが存在しない
            return;
        }

        ttsManager.setReadAroundChannel(guild, channel);

        try {
            audioManager.openAudioConnection(channelUnion);
        } catch (InsufficientPermissionException ex) {
            // 接続失敗
        }
    }

    /**
     * 指定されたチャンネルにBOT以外いないかどうか
     *
     * @param channel オーディオチャンネル
     * @return いればtrue、いなければfalse
     */
    public static boolean isNoUser(AudioChannel channel) {
        return channel.getMembers().stream()
                .allMatch(n -> n.getUser().isBot() || n.getUser().isSystem());
    }

    /**
     * Discordのイベントを受ける取るアダプタ
     *
     * @author MORIMORI0317
     */
    protected final class DiscordEventAdaptor extends ListenerAdapter {

        @Override
        public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
            AudioChannelUnion joinCh = event.getChannelJoined();
            AudioChannelUnion leftCh = event.getChannelLeft();

            if (joinCh == null && leftCh == null) {
                // 多分ありえない
                return;
            }

            Member member = event.getMember();

            JDA jda = event.getJDA();
            Guild guild = Objects.requireNonNull(event.getGuild());
            long guildId = guild.getIdLong();
            DataRepository repo = SaveDataManager.getInstance().getRepository();
            AutoDisconnectMode autoDisMode = repo.getServerData(guildId).getAutoDisconnectMode();

            if (member.getUser().getIdLong() == jda.getSelfUser().getIdLong()) {
                // このBOTがVCから切断、もしくは接続された時に、自動切断の処理を停止
                stopAutoDisconnecter(guildId);

                // このBOTがVCに接続したとき
                if (joinCh != null) {

                    // 再接続を無効化
                    repo.getBotStateData(guildId, ITTSRuntime.getInstance().getBot().getBotId())
                            .setReconnectChannelPair(null);

                    // 誰もいない場合
                    if (autoDisMode.isOn() && isNoUser(joinCh)) {
                        startAutoDisconnecter(guildId);
                    }
                }

                return;
            }

            if (member.getUser().isBot() || member.getUser().isSystem()) {
                // BOTは無視
                return;
            }

            AudioManager audioManager = guild.getAudioManager();
            AudioChannelUnion selfCh = audioManager.getConnectedChannel();

            // ユーザーが参加してきたときの再接続
            if (autoDisMode.isReconnect() && joinCh != null && selfCh == null) {
                TTSChannelPair reconnectChannel = repo
                        .getBotStateData(guildId, ITTSRuntime.getInstance().getBot().getBotId())
                        .getReconnectChannelPair();
                if (reconnectChannel != null && reconnectChannel.readTextChannel() != -1 && reconnectChannel.speakAudioChannel() != -1
                        && reconnectChannel.speakAudioChannel() == joinCh.getIdLong()) {
                    joinReconnect(reconnectChannel, guild, joinCh, audioManager);
                }
            }

            // 自動切断
            if (autoDisMode.isOn() && selfCh != null) {
                long selfChId = selfCh.getIdLong();

                // ユーザーがこのBOTと同じチャンネルから抜けた時
                if (leftCh != null && leftCh.getIdLong() == selfChId) {

                    // Botだけになった場合
                    if (isNoUser(leftCh)) {
                        // 自動切断の処理を開始
                        startAutoDisconnecter(guildId);
                    }

                }

                // ユーザーがこのBOTと同じチャンネルに参加した場合
                if (joinCh != null && joinCh.getIdLong() == selfChId) {
                    // 自動切断の処理を停止
                    stopAutoDisconnecter(guildId);
                }
            }

        }

        @Override
        public void onReady(@NotNull ReadyEvent event) {
            // 起動後の再接続処理
            CompletableFuture.runAsync(() -> {
                long botId = ITTSRuntime.getInstance().getBot().getBotId();
                Map<Long, BotStateData> allData = SaveDataManager.getInstance().getRepository().getAllBotStateData(botId);

                allData.forEach((guildId, data) -> {
                    try {
                        startUpReconnect(guildId, data);
                    } catch (Exception ex) {
                        ITTSRuntime.getInstance().getLogger().error("Reconnection process failed: {}", "GuildID:" + guildId, ex);
                    }
                });

            }, ITTSRuntime.getInstance().getAsyncWorkerExecutor());
        }
    }

    /**
     * 自動切断の制御を行うインスタンス
     *
     * @author MORIMORI0317
     */
    private class AutoDisconnecter {
        /**
         * サーバーID
         */
        private final long guildId;

        /**
         * このインスタンスが終了済みかどうか
         */
        private final AtomicBoolean destroyed = new AtomicBoolean(false);

        /**
         * タイマータスク
         */
        private final AtomicReference<ImmortalityTimer.ImmortalityTimerTask> timerTask = new AtomicReference<>();

        private AutoDisconnecter(long guildId) {
            this.guildId = guildId;
        }

        private void start() {

            // 終了済みであれば処理を終了
            if (destroyed.get()) {
                return;
            }

            // タイマータスクがセットされていれば、開始しているとして処理を終了
            if (!timerTask.compareAndSet(null, createTimerTask())) {
                return;
            }

            // タイマーを開始
            ImmortalityTimer timer = ITTSRuntime.getInstance().getImmortalityTimer();
            timer.schedule(timerTask.get(), DISCONNECT_WAIT_DURATION.toMillis());
        }

        private ImmortalityTimer.ImmortalityTimerTask createTimerTask() {
            return new ImmortalityTimer.ImmortalityTimerTask() {
                @Override
                public void run() {
                    execute();
                }
            };
        }

        private void cancel() {
            // 終了済みであれば処理を終了
            if (!destroyed.compareAndSet(false, true)) {
                return;
            }

            // タイマーをキャンセル
            ImmortalityTimer.ImmortalityTimerTask task = timerTask.get();
            if (task != null) {
                task.cancel();
            }
        }

        private void execute() {
            // 終了済みであれば処理を終了
            if (!destroyed.compareAndSet(false, true)) {
                return;
            }

            DataRepository repo = SaveDataManager.getInstance().getRepository();
            AutoDisconnectMode autoDisMode = repo.getServerData(guildId).getAutoDisconnectMode();
            TTSChannelPair ttsChannel = null;

            // 再接続用のTTSチャンネルペアを取得
            if (autoDisMode.isReconnect()) {
                TTSManager ttsManager = ITTSRuntime.getInstance().getTTSManager();
                TTSInstance ttsInstance = ttsManager.getTTSInstance(guildId);
                if (ttsInstance != null) {
                    ttsChannel = new TTSChannelPair(ttsInstance.getAudioChannel(), ttsInstance.getTextChannel());
                }
            }

            // 切断処理
            JDA jda = ITTSRuntime.getInstance().getBot().getJDA();
            Guild guild = jda.getGuildById(guildId);
            if (guild != null) {
                AudioManager audioManager = guild.getAudioManager();
                audioManager.closeAudioConnection();
            }

            // 再接続先を設定
            if (autoDisMode.isReconnect() && ttsChannel != null) {
                repo.getBotStateData(guildId, ITTSRuntime.getInstance().getBot().getBotId())
                        .setReconnectChannelPair(ttsChannel);
            }

            synchronized (autoDisconnecters) {
                autoDisconnecters.remove(guildId);
            }
        }
    }

}
