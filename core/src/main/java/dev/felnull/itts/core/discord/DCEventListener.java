package dev.felnull.itts.core.discord;

import dev.felnull.itts.core.ITTSRuntimeUse;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * Discordのイベントリスナー
 *
 * @author MORIMORI0317
 */
public class DCEventListener extends ListenerAdapter implements ITTSRuntimeUse {
    /**
     * BOT
     */
    private final Bot bot;

    /**
     * コンストラクタ
     *
     * @param bot BOT
     */
    public DCEventListener(@NotNull Bot bot) {
        this.bot = bot;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        this.bot.baseCommands.stream()
                .filter(n -> n.isCommandMatch(event))
                .limit(1)
                .forEach(r -> r.commandInteraction(event));

    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        this.bot.baseCommands.stream()
                .filter(n -> n.isAutoCompleteMatch(event))
                .limit(1)
                .forEach(r -> r.autoCompleteInteraction(event));
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (getTTSManager().canSpeak(event.getGuild())) {
            getTTSManager().sayChat(event.getGuild(), event.getChannel(), event.getMember(), event.getMessage());
            getTTSManager().sayUploadFile(event.getGuild(), event.getChannel(), event.getMember(), event.getMessage().getAttachments());
        }
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        AudioChannelUnion join = event.getChannelJoined();
        AudioChannelUnion left = event.getChannelLeft();

        if (event.getMember().getUser().getIdLong() == bot.getJDA().getSelfUser().getIdLong()) {
            if (left != null) {
                getTTSManager().disconnect(event.getGuild());
            }

            if (join != null) {
                getTTSManager().connect(event.getGuild(), join);
            }
        } else if (left != null) {
            // 誰かが抜けて、BotだけになったらVCから切断
            boolean isAlone = left.getMembers().stream().allMatch(n -> n.getUser().isBot());
            if (isAlone) {
                left.getGuild().getAudioManager().closeAudioConnection();
            }
        }

        getTTSManager().onVCEvent(event.getGuild(), event.getMember(), join, left);
    }
}
