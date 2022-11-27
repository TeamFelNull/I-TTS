package dev.felnull.ttsvoice.core.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import org.jetbrains.annotations.NotNull;

public class TTSVoiceBot {
    @NotNull
    private final String botToken;
    private JDA jda;

    public TTSVoiceBot(@NotNull String botToken) {
        this.botToken = botToken;
    }

    public void init() {
        this.jda = JDABuilder.createDefault(botToken).addEventListeners(new TTSVoiceBotListener()).build();
        updateCommands(this.jda);
    }

    private static void updateCommands(JDA jda) {
        var membersPermissions = DefaultMemberPermissions.enabledFor(Permission.VOICE_CONNECT, Permission.MESSAGE_SEND);
        var ownersPermissions = DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER);

        var join = Commands.slash("join", "読み上げBOTをVCに呼び出す")
                .addOptions(new OptionData(OptionType.CHANNEL, "channel", "チャンネル指定")
                        .setChannelTypes(ChannelType.VOICE, ChannelType.STAGE))
                .setGuildOnly(true)
                .setDefaultPermissions(membersPermissions);

        var leave = Commands.slash("leave", "読み上げBOTをVCから切断")
                .setGuildOnly(true)
                .setDefaultPermissions(membersPermissions);

        var reconnect = Commands.slash("reconnect", "読み上げBOTをVCに再接続")
                .setGuildOnly(true)
                .setDefaultPermissions(membersPermissions);

        var voice = Commands.slash("voice", "自分の読み上げ音声タイプ関係")
                .setGuildOnly(true)
                .setDefaultPermissions(membersPermissions)
                .addSubcommands(new SubcommandData("change", "自分の読み上げ音声タイプを変更")
                        .addOptions(new OptionData(OptionType.STRING, "voice_category", "読み上げ音声タイプのカテゴリ")
                                .setAutoComplete(true)
                                .setRequired(true))
                        .addOptions(new OptionData(OptionType.STRING, "voice_type", "読み上げ音声タイプ")
                                .setAutoComplete(true)
                                .setRequired(true)))
                .addSubcommands(new SubcommandData("check", "自分の読み上げ音声タイプを確認"))
                .addSubcommands(new SubcommandData("show", "読み上げ音声タイプ一覧を表示"));

        var vnick = Commands.slash("vnick", "自分の読み上げユーザ名を変更")
                .addOptions(new OptionData(OptionType.STRING, "name", "名前")
                        .setRequired(true))
                .setGuildOnly(true)
                .setDefaultPermissions(membersPermissions);

        var info = Commands.slash("info", "情報を表示")
                .setGuildOnly(true)
                .setDefaultPermissions(membersPermissions)
                .addSubcommands(new SubcommandData("about", "BOT情報を表示"))
                .addSubcommands(new SubcommandData("oss", "OSS情報を表示"))
                .addSubcommands(new SubcommandData("work", "稼働情報を表示"));

        var config = Commands.slash("config", "設定")
                .setGuildOnly(true)
                .setDefaultPermissions(ownersPermissions)
                .addSubcommands(new SubcommandData("notify-move", "VCの入退室時にユーザー名を読み上げ")
                        .addOptions(new OptionData(OptionType.BOOLEAN, "enable", "True: 有効、False: 無効")
                                .setRequired(true)))
                .addSubcommands(new SubcommandData("read-limit", "読み上げ文字数上限")
                        .addOptions(new OptionData(OptionType.INTEGER, "max-count", "最大文字数")
                                .setMinValue(1)
                                .setMaxValue(Integer.MAX_VALUE)
                                .setRequired(true)))
                .addSubcommands(new SubcommandData("name-read-limit", "名前の読み上げ文字数上限")
                        .addOptions(new OptionData(OptionType.INTEGER, "max-count", "最大文字数")
                                .setMinValue(1)
                                .setMaxValue(Integer.MAX_VALUE)
                                .setRequired(true)))
                .addSubcommands(new SubcommandData("need-join", "VCに参加中のユーザーのみ読み上げ")
                        .addOptions(new OptionData(OptionType.BOOLEAN, "enable", "True: 有効、False: 無効")
                                .setRequired(true)))
                .addSubcommands(new SubcommandData("read-overwrite", "読み上げの上書き")
                        .addOptions(new OptionData(OptionType.BOOLEAN, "enable", "True: 有効、False: 無効")
                                .setRequired(true)))
                .addSubcommands(new SubcommandData("read-ignore", "読み上げない文字(正規表現)")
                        .addOptions(new OptionData(OptionType.BOOLEAN, "enable", "True: 有効、False: 無効")
                                .setRequired(true)))
                .addSubcommands(new SubcommandData("default-voice", "初期の読み上げタイプ")
                        .addOptions(new OptionData(OptionType.STRING, "voice_category", "読み上げ音声タイプのカテゴリ")
                                .setAutoComplete(true)
                                .setRequired(true))
                        .addOptions(new OptionData(OptionType.STRING, "voice_type", "読み上げ音声タイプ")
                                .setAutoComplete(true)
                                .setRequired(true)))
                .addSubcommands(new SubcommandData("show", "現在のコンフィグを表示"));

        var deny = Commands.slash("deny", "読み上げ拒否関係")
                .setGuildOnly(true)
                .setDefaultPermissions(ownersPermissions)
                .addSubcommands(new SubcommandData("add", "読み上げ拒否リストにユーザーを追加")
                        .addOptions(new OptionData(OptionType.USER, "user", "ユーザー指定")
                                .setRequired(true)))
                .addSubcommands(new SubcommandData("remove", "読み上げ拒否リストからユーザーを削除")
                        .addOptions(new OptionData(OptionType.USER, "user", "ユーザー指定")
                                .setRequired(true)))
                .addSubcommands(new SubcommandData("show", "読み上げ拒否リストを表示"));

        var admin = Commands.slash("admin", "管理者専用")
                .setGuildOnly(true)
                .setDefaultPermissions(ownersPermissions)
                .addSubcommands(new SubcommandData("vnick", "他人の読み上げユーザ名を変更")
                        .addOptions(new OptionData(OptionType.STRING, "name", "名前")
                                .setRequired(true))
                        .addOptions(new OptionData(OptionType.USER, "user", "ユーザー指定")
                                .setRequired(true)))
                .addSubcommandGroups(new SubcommandGroupData("voice", "読み上げ音声タイプ関係")
                        .addSubcommands((new SubcommandData("change", "他人の読み上げ音声タイプを変更")
                                        .addOptions(new OptionData(OptionType.STRING, "voice_category", "読み上げ音声タイプのカテゴリ")
                                                .setAutoComplete(true)
                                                .setRequired(true))
                                        .addOptions(new OptionData(OptionType.STRING, "voice_type", "読み上げ音声タイプ")
                                                .setAutoComplete(true)
                                                .setRequired(true)))
                                        .addOptions(new OptionData(OptionType.USER, "user", "ユーザー指定")
                                                .setRequired(true)),
                                new SubcommandData("check", "他人の読み上げ音声タイプを確認")
                                        .addOptions(new OptionData(OptionType.USER, "user", "ユーザー指定")
                                                .setRequired(true))));

        var dict = Commands.slash("dict", "読み上げ辞書")
                .setGuildOnly(true)
                .setDefaultPermissions(ownersPermissions)
                .addSubcommands(new SubcommandData("toggle", "辞書ごとの有効無効の切り替え")
                        .addOptions(new OptionData(OptionType.STRING, "name", "辞書")
                                .setAutoComplete(true)
                                .setRequired(true))
                        .addOptions(new OptionData(OptionType.BOOLEAN, "enable", "True: 有効、False: 無効")
                                .setRequired(true)))
                .addSubcommands(new SubcommandData("toggle-show", "辞書ごとの有効無効の表示"))
                .addSubcommands(new SubcommandData("show", "サーバー読み上げ辞書の内容を表示"))
                .addSubcommands(new SubcommandData("add", "サーバー読み上げ辞書に単語を登録")
                        .addOption(OptionType.STRING, "word", "対象の単語", true)
                        .addOption(OptionType.STRING, "reading", "対象の読み", true))
                .addSubcommands(new SubcommandData("remove", "サーバー読み上げ辞書から単語を削除")
                        .addOption(OptionType.STRING, "word", "対象の単語", true, true))
                .addSubcommands(new SubcommandData("download", "現在の読み上げ辞書をダウンロード"))
                .addSubcommands(new SubcommandData("upload", "読み上げ辞書をアップロード")
                        .addOption(OptionType.ATTACHMENT, "file", "辞書ファイル", true)
                        .addOption(OptionType.BOOLEAN, "overwrite", "上書き", true));

        jda.updateCommands().addCommands(join, leave, reconnect, voice, vnick, info, config, deny, admin, dict).queue();
    }
}
