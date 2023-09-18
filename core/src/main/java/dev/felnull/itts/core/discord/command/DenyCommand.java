package dev.felnull.itts.core.discord.command;

import dev.felnull.itts.core.savedata.ServerUserData;
import dev.felnull.itts.core.util.DiscordUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * 読み上げ拒否コンフィグ
 *
 * @author MORIMORI0317
 */
public class DenyCommand extends BaseCommand {

    /**
     * コンストラクタ
     */
    public DenyCommand() {
        super("deny");
    }

    @NotNull
    @Override
    public SlashCommandData createSlashCommand() {
        return Commands.slash("deny", "読み上げ拒否関係")
                .setGuildOnly(true)
                .setDefaultPermissions(OWNERS_PERMISSIONS)
                .addSubcommands(new SubcommandData("add", "読み上げ拒否リストにユーザーを追加")
                        .addOptions(new OptionData(OptionType.USER, "user", "ユーザー指定")
                                .setRequired(true)))
                .addSubcommands(new SubcommandData("remove", "読み上げ拒否リストからユーザーを削除")
                        .addOptions(new OptionData(OptionType.USER, "user", "ユーザー指定")
                                .setRequired(true)))
                .addSubcommands(new SubcommandData("show", "読み上げ拒否リストを表示"));
    }

    @Override
    public void commandInteraction(SlashCommandInteractionEvent event) {
        switch (Objects.requireNonNull(event.getSubcommandName())) {
            case "add" -> add(event);
            case "remove" -> remove(event);
            case "show" -> show(event);
            default -> {
            }
        }
    }

    private void remove(SlashCommandInteractionEvent event) {
        Guild guild = Objects.requireNonNull(event.getGuild());
        User user = Objects.requireNonNull(event.getOption("user", OptionMapping::getAsUser));

        if (user.isBot()) {
            event.reply(DiscordUtils.getEscapedName(guild, user) + "はBOTです。").setEphemeral(true).queue();
            return;
        }

        ServerUserData sud = getSaveDataManager().getServerUserData(guild.getIdLong(), event.getUser().getIdLong());
        if (!sud.isDeny()) {
            event.reply("読み上げ拒否をされていないユーザです。").setEphemeral(true).queue();
            return;
        }

        sud.setDeny(false);
        event.reply(DiscordUtils.getEscapedName(guild, user) + "の読み上げ拒否を解除します。").setEphemeral(true).queue();
    }

    private void show(SlashCommandInteractionEvent event) {
        Guild guild = Objects.requireNonNull(event.getGuild());
        List<Long> denyUsers = getSaveDataManager().getAllDenyUser(guild.getIdLong());

        if (denyUsers.isEmpty()) {
            event.reply("読み上げ拒否されたユーザは存在しません。").setEphemeral(true).queue();
            return;
        }

        MessageCreateBuilder msg = new MessageCreateBuilder().addContent("読み上げ拒否されたユーザ一覧\n");
        StringBuilder sb = new StringBuilder();
        for (Long deny : denyUsers) {
            sb.append(DiscordUtils.getEscapedName(guild, Objects.requireNonNull(event.getJDA().getUserById(deny)))).append("\n");
        }
        msg.addContent("``" + sb + "``");
        event.reply(msg.build()).setEphemeral(true).queue();
    }

    private void add(SlashCommandInteractionEvent event) {
        Guild guild = Objects.requireNonNull(event.getGuild());
        User user = event.getOption("user", OptionMapping::getAsUser);
        Objects.requireNonNull(user);

        if (user.isBot()) {
            event.reply(DiscordUtils.getEscapedName(guild, user) + "はBOTです。").setEphemeral(true).queue();
            return;
        }

        ServerUserData sud = getSaveDataManager().getServerUserData(guild.getIdLong(), event.getUser().getIdLong());
        if (sud.isDeny()) {
            event.reply("すでに読み上げ拒否をされているユーザです。").setEphemeral(true).queue();
            return;
        }

        sud.setDeny(true);
        event.reply(DiscordUtils.getEscapedName(guild, user) + "の読み上げ拒否します。").setEphemeral(true).queue();
    }
}
