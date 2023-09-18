package dev.felnull.itts.core.discord.command;

import dev.felnull.itts.core.savedata.ServerUserData;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * ニックネーム変更コマンド
 *
 * @author MORIMORI0317
 */
public class VnickCommand extends BaseCommand {

    /**
     * コンストラクタ
     */
    public VnickCommand() {
        super("vnick");
    }

    @NotNull
    @Override
    public SlashCommandData createSlashCommand() {
        return Commands.slash("vnick", "自分の読み上げユーザ名を変更")
                .addOptions(new OptionData(OptionType.STRING, "name", "名前")
                        .setRequired(true))
                .setGuildOnly(true)
                .setDefaultPermissions(MEMBERS_PERMISSIONS);
    }

    @Override
    public void commandInteraction(SlashCommandInteractionEvent event) {
        Guild guild = Objects.requireNonNull(event.getGuild());
        String name = event.getOption("name", OptionMapping::getAsString);

        ServerUserData sud = getSaveDataManager().getServerUserData(guild.getIdLong(), event.getUser().getIdLong());

        if ("reset".equals(name)) {
            sud.setNickName(null);
            event.reply("自分の読み上げユーザ名をリセットしました。").setEphemeral(true).queue();
        } else {
            sud.setNickName(name);
            event.reply("自分の読み上げユーザ名を変更しました。").setEphemeral(true).queue();
        }

    }
}
