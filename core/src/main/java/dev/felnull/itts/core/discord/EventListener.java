package dev.felnull.itts.core.discord;

import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class EventListener extends ListenerAdapter {
    private final Bot bot;

    public EventListener(@NotNull Bot bot) {
        this.bot = bot;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        this.bot.baseCommands.stream()
                .filter(n -> n.isCommandMatch(event))
                .limit(1)
                .forEach(r -> r.commandInteraction(event));
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        this.bot.baseCommands.stream()
                .filter(n -> n.isAutoCompleteMatch(event))
                .limit(1)
                .forEach(r -> r.autoCompleteInteraction(event));
    }


    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        this.bot.getRuntime().getTTSManager().sayChat(event.getGuild(), event.getChannel(), event.getAuthor(), event.getMember(), event.getMessage().getContentDisplay());
    }


    @Override
    public void onReady(@NotNull ReadyEvent event) {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            var allData = this.bot.getRuntime().getSaveDataManager().getAllBotStateData();
            allData.forEach((guildId, data) -> {
                var guild = bot.getJDA().getGuildById(guildId);

                if (guild != null && data.getConnectedAudioChannel() >= 0 && data.getReadAroundTextChannel() >= 0) {
                    try {
                        var audioChannel = guild.getChannelById(AudioChannel.class, data.getConnectedAudioChannel());
                        if (audioChannel == null) return;

                        var chatChannel = guild.getChannelById(MessageChannel.class, data.getReadAroundTextChannel());
                        if (chatChannel == null) return;

                        bot.getRuntime().getTTSManager().setReadAroundChannel(guild, chatChannel);
                        guild.getAudioManager().openAudioConnection(audioChannel);
                        bot.getRuntime().getTTSManager().connect(guild, audioChannel);
                        bot.getRuntime().getLogger().info("Reconnected: {}", guild.getName());
                    } catch (Exception ex) {
                        bot.getRuntime().getLogger().error("Failed to reconnect: {}", guild.getName());
                    }
                }
            });
        }, bot.getRuntime().getAsyncWorkerExecutor());
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        var join = event.getChannelJoined();
        var left = event.getChannelLeft();

        if (event.getMember().getUser().getIdLong() == bot.getJDA().getSelfUser().getIdLong()) {
            if (left != null)
                bot.getRuntime().getTTSManager().disconnect(event.getGuild());

            if (join != null)
                bot.getRuntime().getTTSManager().connect(event.getGuild(), join);
        }
    }
}
