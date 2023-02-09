package dev.felnull.itts.core.discord;

import dev.felnull.itts.core.ITTSRuntime;
import dev.felnull.itts.core.ITTSRuntimeUse;
import dev.felnull.itts.core.discord.command.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.managers.Presence;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

public class Bot implements ITTSRuntimeUse {
    protected final List<BaseCommand> baseCommands = new ArrayList<>();
    private JDA jda;

    public Bot() {
    }

    public void start() {
        registeringCommands();

        this.jda = JDABuilder.createDefault(getConfigManager().getConfig().getBotToken()).enableIntents(GatewayIntent.MESSAGE_CONTENT).addEventListeners(new EventListener(this)).build();
        updateCommands(this.jda);

        this.jda.getPresence().setStatus(OnlineStatus.ONLINE);
        updateActivity(this.jda.getPresence());

        getITTSTimer().schedule(new TimerTask() {
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
    }

    private void registerCommand(BaseCommand command) {
        baseCommands.add(command);
    }

    private void updateCommands(JDA jda) {
        jda.updateCommands().addCommands(baseCommands.stream().map(BaseCommand::createSlashCommand).toList()).queue();
    }

    public void updateActivityAsync() {
        CompletableFuture.runAsync(() -> updateActivity(jda.getPresence()), getAsyncExecutor());
    }

    public void updateActivity(Presence presence) {
        var vstr = getITTSRuntime().getVersionText();
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
}
