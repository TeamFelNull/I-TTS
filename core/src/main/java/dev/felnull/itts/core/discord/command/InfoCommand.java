package dev.felnull.itts.core.discord.command;

import dev.felnull.fnjl.util.FNStringUtil;
import dev.felnull.itts.core.ITTSRuntime;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

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
        switch (Objects.requireNonNull(event.getSubcommandName())) {
            case "about" -> about(event);
            case "oss" -> oss(event);
            case "work" -> work(event);
        }
    }

    private void about(SlashCommandInteractionEvent e) {
        EmbedBuilder aboutEmbedBuilder = new EmbedBuilder();
        aboutEmbedBuilder.setColor(getRuntime().getConfigManager().getConfig().getThemeColor());
        aboutEmbedBuilder.setTitle("Ikisugi TTS BOT");
        aboutEmbedBuilder.setDescription(getRuntime().getVersionText());
        aboutEmbedBuilder.addField("License", "GNU LGPLv3", false);
        aboutEmbedBuilder.setFooter("Developed by FelNull", "https://avatars.githubusercontent.com/u/59995376?s=200&v=4");

        e.replyEmbeds(aboutEmbedBuilder.build()).addActionRow(Button.of(ButtonStyle.LINK, "https://github.com/TeamFelnull/IDiscordTTSVoice", "Source")).setEphemeral(true).queue();
    }

    private void oss(SlashCommandInteractionEvent e) {
        EmbedBuilder ossEmbedBuilder = new EmbedBuilder();
        ossEmbedBuilder.setColor(getRuntime().getConfigManager().getConfig().getThemeColor());

        ossEmbedBuilder.setTitle("OSSクレジット");

        ossEmbedBuilder.addField("VOICEVOX", "voicevox.hiroshiba.jp", false);
        ossEmbedBuilder.addField("COEIROINK", "coeiroink.com", false);
        ossEmbedBuilder.addField("SHAREVOX", "sharevox.app", false);

        ossEmbedBuilder.addField("VoiceTextWebAPI", "cloud.voicetext.jp", false);

        e.replyEmbeds(ossEmbedBuilder.build()).setEphemeral(true).queue();
    }

    private void work(SlashCommandInteractionEvent e) {
        EmbedBuilder workEmbedBuilder = new EmbedBuilder();
        workEmbedBuilder.setColor(getRuntime().getConfigManager().getConfig().getThemeColor());

        workEmbedBuilder.setTitle("稼働情報");

        workEmbedBuilder.addField("稼働時間", FNStringUtil.getTimeFormat(System.currentTimeMillis() - ITTSRuntime.getInstance().getStartupTime()), false);
        workEmbedBuilder.addField("参加サーバー数", e.getJDA().getGuilds().size() + "個", false);
        workEmbedBuilder.addField("読み上げサーバー数", getRuntime().getTTSManager().getTTSCount() + "個", false);

        e.replyEmbeds(workEmbedBuilder.build()).setEphemeral(true).queue();
    }
}
