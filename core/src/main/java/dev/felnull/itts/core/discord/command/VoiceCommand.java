package dev.felnull.itts.core.discord.command;

import com.google.common.collect.ImmutableList;
import dev.felnull.itts.core.ITTSRuntime;
import dev.felnull.itts.core.savedata.ServerUserData;
import dev.felnull.itts.core.util.DiscordUtils;
import dev.felnull.itts.core.util.StringUtils;
import dev.felnull.itts.core.voice.VoiceType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

public class VoiceCommand extends BaseCommand {
    public VoiceCommand() {
        super("voice");
    }

    @NotNull
    @Override
    public SlashCommandData createSlashCommand() {
        return Commands.slash("voice", "自分の読み上げ音声タイプ関係")
                .setGuildOnly(true)
                .setDefaultPermissions(MEMBERS_PERMISSIONS)
                .addSubcommands(new SubcommandData("change", "自分の読み上げ音声タイプを変更")
                        .addOptions(new OptionData(OptionType.STRING, "voice_category", "読み上げ音声タイプのカテゴリ")
                                .setAutoComplete(true)
                                .setRequired(true))
                        .addOptions(new OptionData(OptionType.STRING, "voice_type", "読み上げ音声タイプ")
                                .setAutoComplete(true)
                                .setRequired(true)))
                .addSubcommands(new SubcommandData("check", "自分の読み上げ音声タイプを確認"))
                .addSubcommands(new SubcommandData("show", "読み上げ音声タイプ一覧を表示"));
    }

    @Override
    public void commandInteraction(SlashCommandInteractionEvent event) {
        switch (Objects.requireNonNull(event.getSubcommandName())) {
            case "change" -> change(event, null);
            case "check" -> check(event, null);
            case "show" -> show(event);
        }
    }

    private void show(SlashCommandInteractionEvent event) {
        EmbedBuilder showEmbedBuilder = new EmbedBuilder();
        showEmbedBuilder.setColor(getConfigManager().getConfig().getThemeColor());
        showEmbedBuilder.setTitle("読み上げ音声タイプ一覧");

        var vm = getVoiceManager();
        var currentVt = vm.getVoiceType(event.getGuild().getIdLong(), event.getUser().getIdLong());
        var defaultVt = vm.getDefaultVoiceType(event.getGuild().getIdLong());
        var catAndTypes = vm.getAvailableVoiceTypes();
        catAndTypes.forEach((cat, types) -> {
            StringBuilder typeText = new StringBuilder();

            for (VoiceType type : types) {
                String name = type.getName();

                if (defaultVt != null && type.getId().equals(defaultVt.getId()))
                    name += " [デフォルト]";

                if (currentVt != null && type.getId().equals(currentVt.getId()))
                    name += " [使用中]";

                typeText.append(name).append("\n");
            }

            showEmbedBuilder.addField(cat.getName(), typeText.toString(), false);
        });

        event.replyEmbeds(showEmbedBuilder.build()).setEphemeral(true).queue();
    }

    protected static void change(SlashCommandInteractionEvent event, User user) {
        boolean mine = user == null;
        if (mine)
            user = event.getUser();

        var sdm = ITTSRuntime.getInstance().getSaveDataManager();

        var serverUserData = sdm.getServerUserData(event.getGuild().getIdLong(), user.getIdLong());
        var odVc = Objects.requireNonNull(event.getOption("voice_category"));
        var odVt = Objects.requireNonNull(event.getOption("voice_type"));

        var vm = ITTSRuntime.getInstance().getVoiceManager();
        var cat = vm.getVoiceCategory(odVc.getAsString());

        if (cat.isEmpty()) {
            event.reply("存在しない読み上げカテゴリーです。").setEphemeral(true).queue();
            return;
        }

        var vt = vm.getVoiceType(odVt.getAsString());

        if (vt.isEmpty()) {
            event.reply("存在しない読み上げタイプです。").setEphemeral(true).queue();
            return;
        }

        if (vt.get().getId().equals(serverUserData.getVoiceType())) {
            event.reply("自分の読み上げ音声タイプは変更されませんでした。").setEphemeral(true).queue();
            return;
        }

        serverUserData.setVoiceType(vt.get().getId());

        String userName = mine ? "自分" : DiscordUtils.getEscapedName(event.getGuild(), user);
        event.reply(userName + "の読み上げ音声タイプを" + vt.get().getName() + "に変更しました。").setEphemeral(mine).queue();
    }

    protected static void check(SlashCommandInteractionEvent event, User user) {
        boolean mine = user == null;
        if (mine)
            user = event.getUser();

        ServerUserData serverUserData = ITTSRuntime.getInstance().getSaveDataManager().getServerUserData(event.getGuild().getIdLong(), user.getIdLong());
        var vm = ITTSRuntime.getInstance().getVoiceManager();
        Optional<VoiceType> vt = vm.getVoiceType(serverUserData.getVoiceType());

        String type = vt.map(VoiceType::getName).orElseGet(() -> {
            var dvt = vm.getDefaultVoiceType(event.getGuild().getIdLong());
            if (dvt != null)
                return dvt.getName() + " [デフォルト]";

            return "無効";
        });

        String userName = mine ? "自分" : DiscordUtils.getEscapedName(event.getGuild(), user);
        event.reply(userName + "の現在の読み上げタイプは" + type + "です。").setEphemeral(mine).queue();
    }

    @Override
    public void autoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        Objects.requireNonNull(event.getGuild());
        var interact = event.getInteraction();

        if (!"change".equals(interact.getSubcommandName())) return;

        voiceSelectComplete(event, null, true);
    }

    protected static void voiceSelectComplete(CommandAutoCompleteInteractionEvent event, User user, boolean showUsed) {
        if (user == null)
            user = event.getUser();

        var interact = event.getInteraction();

        var fcs = interact.getFocusedOption();
        var val = fcs.getValue();

        var vm = ITTSRuntime.getInstance().getVoiceManager();
        var catAndTypes = vm.getAvailableVoiceTypes();

        if ("voice_category".equals(fcs.getName())) {

            event.replyChoices(catAndTypes.keySet().stream()
                    .sorted(Comparator.comparingInt(cat -> -StringUtils.getComplementPoint(cat.getName(), val)))
                    .limit(OptionData.MAX_CHOICES)
                    .map(cat -> new Command.Choice(cat.getName(), cat.getId()))
                    .toList()).queue();

        } else if ("voice_type".equals(fcs.getName())) {
            var currentVt = showUsed ? vm.getVoiceType(event.getGuild().getIdLong(), user.getIdLong()) : null;

            var defaultVt = vm.getDefaultVoiceType(event.getGuild().getIdLong());

            var cat = Optional.ofNullable(event.getOption("voice_category"))
                    .flatMap(catOp -> vm.getVoiceCategory(catOp.getAsString()));

            event.replyChoices(cat.map(catAndTypes::get)
                    .map(vts -> vts.stream()
                            .sorted(Comparator.comparingInt(vt -> -StringUtils.getComplementPoint(vt.getName(), val)))
                            .limit(OptionData.MAX_CHOICES)
                            .map(vt -> {
                                var name = vt.getName();
                                if (defaultVt != null && vt.getId().equals(defaultVt.getId()))
                                    name += " [デフォルト]";

                                if (showUsed && currentVt != null && vt.getId().equals(currentVt.getId()))
                                    name += " [使用中]";

                                return new Command.Choice(name, vt.getId());
                            })
                            .toList())
                    .orElseGet(ImmutableList::of)).queue();
        }
    }
}
