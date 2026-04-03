package dev.felnull.itts.core.discord;

import dev.felnull.itts.core.ITTSRuntimeUse;
import dev.felnull.itts.core.ImmortalityTimer;
import dev.felnull.itts.core.discord.command.*;
import club.minnced.discord.jdave.interop.JDaveSessionFactory;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.audio.AudioModuleConfig;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.managers.Presence;
import net.dv8tion.jda.api.requests.GatewayIntent;

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
     * 接続制御
     */
    private final ConnectControl connectControl = new ConnectControl();

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
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new DCEventListener(this), this.connectControl.getAdaptor())
                .setAudioModuleConfig(new AudioModuleConfig().withDaveSessionFactory(new JDaveSessionFactory()))
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

    public ConnectControl getConnectControl() {
        return connectControl;
    }
}
