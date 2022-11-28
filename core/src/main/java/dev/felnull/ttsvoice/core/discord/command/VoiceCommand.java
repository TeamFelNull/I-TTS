package dev.felnull.ttsvoice.core.discord.command;

import dev.felnull.ttsvoice.core.TTSVoiceRuntime;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

public class VoiceCommand extends BaseCommand {
    public VoiceCommand(@NotNull TTSVoiceRuntime runtime) {
        super(runtime, "voice");
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

    }
}
