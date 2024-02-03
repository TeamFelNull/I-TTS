package dev.felnull.itts.core.discord.command;

import dev.felnull.itts.core.tts.TTSManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * 情報表示コマンド
 *
 * @author MORIMORI0317
 */
public class InfoCommand extends BaseCommand {

    /**
     * ソースコードのURL
     */
    private static final String SOURCE_URL = "https://github.com/TeamFelnull/I-TTS";

    /**
     * 時間を相対的に表示するフォーマット
     */
    private static final String RELATIVE_TIME_FORMAT = "<t:%d:R>";

    /**
     * コンストラクタ
     */
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
            default -> {
            }
        }
    }

    private void about(SlashCommandInteractionEvent e) {
        EmbedBuilder aboutEmbedBuilder = new EmbedBuilder();
        aboutEmbedBuilder.setColor(getConfigManager().getConfig().getThemeColor());
        aboutEmbedBuilder.setTitle("Ikisugi TTS BOT");
        aboutEmbedBuilder.setDescription(getITTSRuntime().getVersionText());
        aboutEmbedBuilder.addField("License", "GNU LGPLv3", false);
        aboutEmbedBuilder.setFooter("Developed by FelNull", "https://avatars.githubusercontent.com/u/59995376?s=200&v=4");

        e.replyEmbeds(aboutEmbedBuilder.build()).addActionRow(Button.of(ButtonStyle.LINK, SOURCE_URL, "Source")).setEphemeral(true).queue();
    }

    private void oss(SlashCommandInteractionEvent e) {
        EmbedBuilder ossEmbedBuilder = new EmbedBuilder();
        ossEmbedBuilder.setColor(getConfigManager().getConfig().getThemeColor());

        ossEmbedBuilder.setTitle("OSSクレジット");

        ossEmbedBuilder.addField("VOICEVOX", "voicevox.hiroshiba.jp", false);
        ossEmbedBuilder.addField("COEIROINK", "coeiroink.com", false);
        ossEmbedBuilder.addField("SHAREVOX", "sharevox.app", false);

        ossEmbedBuilder.addField("VoiceTextWebAPI", "cloud.voicetext.jp", false);

        e.replyEmbeds(ossEmbedBuilder.build()).setEphemeral(true).queue();
    }

    private void work(SlashCommandInteractionEvent e) {
        TTSManager ttsManager = getTTSManager();

        EmbedBuilder workEmbedBuilder = new EmbedBuilder();
        workEmbedBuilder.setColor(getConfigManager().getConfig().getThemeColor());

        workEmbedBuilder.setTitle("稼働情報");

        workEmbedBuilder.addField("稼働開始時間", String.format(RELATIVE_TIME_FORMAT, getITTSRuntime().getStartupTime() / 1000), false);
        workEmbedBuilder.addField("参加サーバー数", e.getJDA().getGuilds().size() + "個", false);
        workEmbedBuilder.addField("読み上げサーバー数", ttsManager.getTTSCount() + "個", false);
        workEmbedBuilder.addField("利用者数", ttsManager.getUserCount() + "人", false);

        e.replyEmbeds(workEmbedBuilder.build()).setEphemeral(true).queue();
    }
}
