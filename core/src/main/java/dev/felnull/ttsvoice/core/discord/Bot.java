package dev.felnull.ttsvoice.core.discord;

import dev.felnull.ttsvoice.core.TTSVoiceRuntime;
import dev.felnull.ttsvoice.core.discord.command.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.ArrayList;
import java.util.List;

public class Bot {
    protected final List<BaseCommand> baseCommands = new ArrayList<>();
    private JDA jda;

    public Bot() {
    }

    public void init() {
        registeringCommands();

        this.jda = JDABuilder.createDefault(getRuntime().getConfigManager().getConfig().getBotToken()).enableIntents(GatewayIntent.MESSAGE_CONTENT).addEventListeners(new EventListener(this)).build();
        updateCommands(this.jda);
    }

    public TTSVoiceRuntime getRuntime() {
        return TTSVoiceRuntime.getInstance();
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

    public JDA getJDA() {
        return jda;
    }
}
