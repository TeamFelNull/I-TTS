package dev.felnull.itts.core.discord.command;

import dev.felnull.itts.core.savedata.ServerUserData;
import dev.felnull.itts.core.util.DiscordUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Adminコマンド
 *
 * @author MORIMORI0317
 */
public class AdminCommand extends BaseCommand {

    /**
     * コンストラクタ
     */
    public AdminCommand() {
        super("admin");
    }

    @NotNull
    @Override
    public SlashCommandData createSlashCommand() {
        return Commands.slash("admin", "管理者専用")
                .setGuildOnly(true)
                .setDefaultPermissions(OWNERS_PERMISSIONS)
                .addSubcommands(new SubcommandData("vnick", "他人の読み上げユーザ名を変更")
                        .addOptions(new OptionData(OptionType.USER, "user", "ユーザー指定")
                                .setRequired(true))
                        .addOptions(new OptionData(OptionType.STRING, "name", "名前")
                                .setRequired(true)))
                .addSubcommandGroups(new SubcommandGroupData("voice", "読み上げ音声タイプ関係")
                        .addSubcommands((new SubcommandData("change", "他人の読み上げ音声タイプを変更")
                                        .addOptions(new OptionData(OptionType.USER, "user", "ユーザー指定")
                                                .setRequired(true))
                                        .addOptions(new OptionData(OptionType.STRING, "voice_category", "読み上げ音声タイプのカテゴリ")
                                                .setAutoComplete(true)
                                                .setRequired(true))
                                        .addOptions(new OptionData(OptionType.STRING, "voice_type", "読み上げ音声タイプ")
                                                .setAutoComplete(true)
                                                .setRequired(true))),
                                new SubcommandData("check", "他人の読み上げ音声タイプを確認")
                                        .addOptions(new OptionData(OptionType.USER, "user", "ユーザー指定")
                                                .setRequired(true))));
    }

    @Override
    public void commandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if ("vnick".equals(event.getSubcommandName())) {
            vnick(event);
        } else if ("voice".equals(event.getSubcommandGroup())) {
            if ("change".equals(event.getSubcommandName())) {
                voiceChange(event);
            } else if ("check".equals(event.getSubcommandName())) {
                voiceCheck(event);
            }
        }
    }

    private void vnick(SlashCommandInteractionEvent event) {
        User user = Objects.requireNonNull(event.getOption("user", OptionMapping::getAsUser));
        String name = Objects.requireNonNull(event.getOption("name", OptionMapping::getAsString));
        Guild guild = Objects.requireNonNull(event.getGuild());
        ServerUserData sud = getSaveDataManager().getServerUserData(guild.getIdLong(), user.getIdLong());

        if ("reset".equals(name)) {
            sud.setNickName(null);
            event.reply(DiscordUtils.getEscapedName(event.getGuild(), user) + "の読み上げユーザ名をリセットしました。").queue();
        } else {
            sud.setNickName(name);
            event.reply(DiscordUtils.getEscapedName(event.getGuild(), user) + "の読み上げユーザ名を変更しました。").queue();
        }
    }

    private void voiceChange(SlashCommandInteractionEvent event) {
        User user = Objects.requireNonNull(event.getOption("user", OptionMapping::getAsUser));
        VoiceCommand.change(event, user);
    }

    private void voiceCheck(SlashCommandInteractionEvent event) {
        User user = Objects.requireNonNull(event.getOption("user", OptionMapping::getAsUser));
        VoiceCommand.check(event, user);
    }

    @Override
    public void autoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if (!"voice".equals(event.getSubcommandGroup()) || !"change".equals(event.getSubcommandName())) {
            return;
        }

        VoiceCommand.voiceSelectComplete(event, null, false);
    }
}
