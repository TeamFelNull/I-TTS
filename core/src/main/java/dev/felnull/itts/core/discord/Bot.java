package dev.felnull.itts.core.discord;

import dev.felnull.itts.core.ITTSRuntimeUse;
import dev.felnull.itts.core.ImmortalityTimer;
import dev.felnull.itts.core.discord.command.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.managers.Presence;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * BOT管理
 *
 * @author MORIMORI0317
 */
public class Bot implements ITTSRuntimeUse {
    /**
     * 全コマンド
     */
    protected final List<BaseCommand> baseCommands = new ArrayList<>();

    /**
     * JDA
     */
    private JDA jda;

    /**
     * BOTを開始
     */
    public void start() {
        registeringCommands();

        this.jda = JDABuilder.createDefault(getConfigManager().getConfig().getBotToken())
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_PRESENCES)
                .enableCache(CacheFlag.ACTIVITY)
                .addEventListeners(new DCEventListener(this))
                .build();

        updateCommands(this.jda);

        this.jda.getPresence().setStatus(OnlineStatus.ONLINE);
        updateActivity(this.jda.getPresence());

        getImmortalityTimer().schedule(new ImmortalityTimer.ImmortalityTimerTask() {
            @Override
            public void run() {
                updateActivityAsync();
            }
        }, 0, 1000 * 10);

        try {
            this.jda.awaitReady();
            reconnect();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void reconnect() {
        /*CompletableFuture.runAsync(() -> {
            long botId = ITTSRuntime.getInstance().getBot().getJDA().getSelfUser().getIdLong();
            Map<Long, TTSChannelPair> connectedChannels = SaveDataManager.getInstance().getRepository().getAllConnectedChannel(botId);
            long selfId = getJDA().getSelfUser().getIdLong();

            connectedChannels.forEach((guildId, data) -> {
                Guild guild = getJDA().getGuildById(guildId);

                if (guild != null && data.getConnectedAudioChannel() >= 0 && data.getReadAroundTextChannel() >= 0) {
                    try {
                        AudioChannel audioChannel = guild.getChannelById(AudioChannel.class, data.getConnectedAudioChannel());

                        if (audioChannel == null) {
                            data.setConnectedAudioChannel(-1);
                            getITTSLogger().info("Failed to reconnect (Audio channel does not exist): {}", guild.getName());
                            return;
                        }

                        MessageChannel chatChannel = guild.getTextChannelById(data.getReadAroundTextChannel());

                        if (chatChannel == null) {
                            data.setReadAroundTextChannel(-1);
                            data.setConnectedAudioChannel(-1);
                            getITTSLogger().info("Failed to reconnect (Message channel does not exist): {}", guild.getName());
                            return;
                        }

                        getTTSManager().setReadAroundChannel(guild, chatChannel);

                        try {
                            guild.getAudioManager().openAudioConnection(audioChannel);
                        } catch (InsufficientPermissionException ex) {
                            data.setConnectedAudioChannel(-1);
                            getITTSLogger().info("Failed to reconnect (No permission): {}", guild.getName());
                            return;
                        }

                        getTTSManager().connect(guild, audioChannel);

                        TTSInstance ti = getTTSManager().getTTSInstance(guildId);
                        VoiceType vt = getVoiceManager().getVoiceType(guildId, selfId);

                        if (ti != null && vt != null) {
                            if (getTTSManager().canSpeak(guild)) {
                                ti.sayText(new StartupSaidText(vt.createVoice(guildId, selfId)));
                            }
                        }

                        getITTSLogger().info("Reconnected: {}", guild.getName());
                    } catch (Exception ex) {
                        getITTSLogger().error("Failed to reconnect: {}", guild.getName(), ex);
                    }
                }
            });

        }, getAsyncExecutor());*/
    }

    private void registeringCommands() {
        registerCommand(new JoinCommand());
        registerCommand(new LeaveCommand());
        registerCommand(new ReconnectCommand());
        registerCommand(new VoiceCommand());
        registerCommand(new VnickCommand());
        registerCommand(new InfoCommand());
        registerCommand(new ConfigCommand());
        registerCommand(new DenyCommand());
        registerCommand(new AdminCommand());
        registerCommand(new DictCommand());
        registerCommand(new SkipCommand());
    }

    private void registerCommand(BaseCommand command) {
        baseCommands.add(command);
    }

    private void updateCommands(JDA jda) {
        jda.updateCommands().addCommands(baseCommands.stream().map(BaseCommand::createSlashCommand).toList()).queue();
    }

    /**
     * 非同期にアクティビティを更新
     */
    public void updateActivityAsync() {
        CompletableFuture.runAsync(() -> updateActivity(jda.getPresence()), getAsyncExecutor());
    }

    /**
     * アクティビティを更新
     *
     * @param presence プレセンス
     */
    public void updateActivity(Presence presence) {
        String vstr = getITTSRuntime().getVersionText();
        int ct = getTTSManager().getTTSCount();

        if (ct > 0) {
            presence.setActivity(Activity.listening(vstr + " - " + ct + "個のサーバーで読み上げ"));
        } else {
            presence.setActivity(Activity.playing(vstr + " - " + "待機"));
        }
    }

    public JDA getJDA() {
        return jda;
    }

    public long getBotId() {
        return jda.getSelfUser().getIdLong();
    }
}
