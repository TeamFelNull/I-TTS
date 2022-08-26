package dev.felnull.ttsvoice.tts;

import dev.felnull.ttsvoice.tts.dictionary.DictionaryManager;
import dev.felnull.ttsvoice.util.TextUtils;
import dev.felnull.ttsvoice.voice.HasTitleAndID;
import dev.felnull.ttsvoice.voice.VoiceCategory;
import dev.felnull.ttsvoice.voice.VoiceType;
import dev.felnull.ttsvoice.voice.reinoare.ReinoareEntry;
import dev.felnull.ttsvoice.voice.reinoare.ReinoareManager;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.ArrayList;

public class TTSCommandAutoCompletes {
    public static void voiceChange(CommandAutoCompleteInteractionEvent e) {
        var opc = e.getInteraction().getOption("voice_category");
        var opt = e.getInteraction().getOption("voice_type");
        String strc = opc == null ? "" : opc.getAsString();
        String strt = opt == null ? null : opt.getAsString();
        var choices = new ArrayList<HasTitleAndID>();
        if (strt == null) {
            for (VoiceCategory voiceCategory : TTSManager.getInstance().getVoiceCategories(e.getUser().getIdLong(), e.getGuild().getIdLong())) {
                if (voiceCategory.getId().contains(strc) || voiceCategory.getTitle().contains(strc))
                    choices.add(voiceCategory);
            }
        } else {
            var cat = TTSManager.getInstance().getVoiceCategories(e.getUser().getIdLong(), e.getGuild().getIdLong()).stream().filter(n -> n.getId().equalsIgnoreCase(strc)).findFirst();

            cat.ifPresent(c -> {
                for (VoiceType voiceType : TTSManager.getInstance().getVoiceTypes(e.getUser().getIdLong(), e.getGuild().getIdLong())) {
                    if (voiceType.getId().contains(strc) && voiceType.getCategory() == c)
                        choices.add(voiceType);
                }
            });
        }

        e.replyChoices(choices.stream().limit(25).map(n -> new Command.Choice(n.getTitle(), n.getId())).toList()).queue();
    }

    public static void playReinoare(CommandAutoCompleteInteractionEvent e, ReinoareManager reinoareManager) {
        var op = e.getInteraction().getOption("search");
        var entries = new ArrayList<ReinoareEntry>();
        if (op != null && reinoareManager.isEnable(e.getGuild().getIdLong())) {
            try {
                var scr = reinoareManager.search(op.getAsString(), 25);
                scr = reinoareManager.sort(scr);
                entries.addAll(scr);
            } catch (Exception ignored) {
            }
        }

        e.replyChoices(entries.stream().map(n -> new Command.Choice(n.getName(), n.getName())).toList()).queue();
    }

    public static void dictRemove(CommandAutoCompleteInteractionEvent e) {
        DictionaryManager dm = DictionaryManager.getInstance();
        var gd = dm.getGuildDictionary(e.getGuild().getIdLong());

        String str = null;
        var strOp = e.getOption("word");
        if (strOp != null)
            str = strOp.getAsString();

        String finalStr = str;
        e.replyChoices(gd.getEntryShowTexts().keySet().stream().sorted((o1, o2) -> {
            if (finalStr == null) return 0;

            int p1 = TextUtils.getComplementPoint(o1, finalStr);
            int p2 = TextUtils.getComplementPoint(o2, finalStr);
            return p2 - p1;
        }).limit(25).map(n -> new Command.Choice(n, n)).toList()).queue();
    }
}
