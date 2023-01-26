package dev.felnull.ttsvoice.core.discord.command;

import com.google.common.collect.ImmutableList;
import dev.felnull.ttsvoice.core.util.StringUtils;
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

    }

    @Override
    public void autoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        var interact = event.getInteraction();

        if (!"change".equals(interact.getSubcommandName())) return;

        var fcs = interact.getFocusedOption();
        var val = fcs.getValue();

        var catAndTypes = getRuntime().getVoiceManager().getAvailableVoiceTypes();

        if ("voice_category".equals(fcs.getName())) {

            event.replyChoices(catAndTypes.keySet().stream()
                    .sorted(Comparator.comparingInt(cat -> -StringUtils.getComplementPoint(cat.getName(), val)))
                    .limit(OptionData.MAX_CHOICES)
                    .map(cat -> new Command.Choice(cat.getName(), cat.getId()))
                    .toList()).queue();

        } else if ("voice_type".equals(fcs.getName())) {

            var cat = Optional.ofNullable(event.getOption("voice_category"))
                    .flatMap(catOp -> catAndTypes.keySet().stream()
                            .filter(r -> r.getId().equals(catOp.getAsString()))
                            .findAny());

            event.replyChoices(cat.map(catAndTypes::get)
                    .map(vts -> vts.stream()
                            .sorted(Comparator.comparingInt(vt -> -StringUtils.getComplementPoint(vt.getName(), val)))
                            .limit(OptionData.MAX_CHOICES)
                            .map(vt -> new Command.Choice(vt.getName(), vt.getId()))
                            .toList())
                    .orElseGet(ImmutableList::of)).queue();
        }
    }
}
