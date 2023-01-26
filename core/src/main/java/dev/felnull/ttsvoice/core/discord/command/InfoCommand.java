package dev.felnull.ttsvoice.core.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.jetbrains.annotations.NotNull;

public class InfoCommand extends BaseCommand {
    public InfoCommand() {
        super("info");
    }

    @NotNull
    @Override
    public SlashCommandData createSlashCommand() {
        return Commands.slash("info", "情報を表示")
                .setGuildOnly(true)
                .setDefaultPermissions(MEMBERS_PERMISSIONS)
                .addSubcommands(new SubcommandData("about", "BOT情報を表示"))
                .addSubcommands(new SubcommandData("oss", "OSS情報を表示"))
                .addSubcommands(new SubcommandData("work", "稼働情報を表示"));
    }

    @Override
    public void commandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getSubcommandName()) {
            case "about" -> about(event);
            case "oss" -> oss(event);
            case "work" -> work(event);
        }
    }

    private void about(SlashCommandInteractionEvent e) {
        EmbedBuilder aboutEmbedBuilder = new EmbedBuilder();
        aboutEmbedBuilder.setColor(getRuntime().getConfigManager().getConfig().getThemeColor());
        aboutEmbedBuilder.setTitle("I Discord TTS Voice BOT");
        aboutEmbedBuilder.setDescription(getRuntime().getVersionText());
        aboutEmbedBuilder.addField("License", "GNU LGPLv3", false);
        aboutEmbedBuilder.setFooter("Developed by FelNull", "https://avatars.githubusercontent.com/u/59995376?s=200&v=4");

        e.replyEmbeds(aboutEmbedBuilder.build()).addActionRow(Button.of(ButtonStyle.LINK, "https://github.com/TeamFelnull/IDiscordTTSVoice", "Source")).queue();
    }

    private void oss(SlashCommandInteractionEvent e) {

    }

    private void work(SlashCommandInteractionEvent e) {

    }
}
