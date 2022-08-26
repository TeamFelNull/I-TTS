package dev.felnull.ttsvoice;

import com.google.common.collect.ImmutableList;
import dev.felnull.ttsvoice.audio.loader.VoiceLoaderManager;
import dev.felnull.ttsvoice.data.Config;
import dev.felnull.ttsvoice.data.ConfigAndSaveDataManager;
import dev.felnull.ttsvoice.data.SaveData;
import dev.felnull.ttsvoice.data.ServerSaveData;
import dev.felnull.ttsvoice.data.dictionary.DictionaryManager;
import dev.felnull.ttsvoice.discord.JDAManager;
import dev.felnull.ttsvoice.tts.TTSListener;
import dev.felnull.ttsvoice.tts.TTSManager;
import dev.felnull.ttsvoice.util.DiscordUtils;
import dev.felnull.ttsvoice.voice.googletranslate.GoogleTranslateTTSManager;
import dev.felnull.ttsvoice.voice.reinoare.ReinoareManager;
import dev.felnull.ttsvoice.voice.voicetext.VoiceTextManager;
import dev.felnull.ttsvoice.voice.vvengine.coeiroink.CoeiroInkManager;
import dev.felnull.ttsvoice.voice.vvengine.voicevox.VoiceVoxManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger(Main.class);
    private static final Timer TIMER = new Timer("main-timer", true);
    public static final long START_TIME = System.currentTimeMillis();
    public static final int CONFIG_VERSION = 1;
    public static final int THEME_COLOR = 0x114514;
    public static String VERSION;

    public static void main(String[] args) throws Exception {
        VERSION = Main.class.getPackage().getImplementationVersion();
        if (VERSION == null) VERSION = "NONE";

        LOGGER.info("The Ikisugi Discord TTS BOT v" + VERSION);
        LOGGER.info("--System info--");
        LOGGER.info("Java version: " + System.getProperty("java.version"));
        LOGGER.info("Java vm name: " + System.getProperty("java.vm.name"));
        LOGGER.info("Java vm version: " + System.getProperty("java.vm.version"));
        LOGGER.info("OS: " + System.getProperty("os.name"));
        LOGGER.info("Arch: " + System.getProperty("os.arch"));
        LOGGER.info("Available Processors: " + Runtime.getRuntime().availableProcessors());
        LOGGER.info("---------------");

        if (!ConfigAndSaveDataManager.getInstance().init(TIMER))
            return;

        DictionaryManager.getInstance().init();
        VoiceLoaderManager.getInstance().init(TIMER);

        JDAManager.getInstance().init(jda -> {
            jda.addEventListener(new TTSListener());
            updateGlobalCommands(jda);
        });

        TimerTask updatePresenceTask = new TimerTask() {
            public void run() {
                updatePresence();
            }
        };
        TIMER.scheduleAtFixedRate(updatePresenceTask, 1000 * 30, 1000 * 30);

        VoiceVoxManager.ALIVE_CHECKER.init(TIMER);
        CoeiroInkManager.ALIVE_CHECKER.init(TIMER);
        VoiceTextManager.ALIVE_CHECKER.init(TIMER);
        GoogleTranslateTTSManager.ALIVE_CHECKER.init(TIMER);
        ReinoareManager.ALIVE_CHECKER.init(TIMER);
    }

    public static void updateAllGuildCommand() {
        List<Guild> guilds = new ArrayList<>();

        var jdas = JDAManager.getInstance().getAllJDA();
        synchronized (jdas) {
            for (JDA jda : jdas) {
                guilds.addAll(jda.getGuilds());
            }
        }

        for (Guild guild : guilds) {
            updateGuildCommand(guild, false);
        }
    }

    public static void updateGuildCommand(Guild guild, boolean allJDA) {
        long guildId = guild.getIdLong();
        var defaultPermissions = getUseCommandPermission();

        List<CommandData> cmds = new ArrayList<>();

        if (!DiscordUtils.isNonAllowInm(guildId) && getServerSaveData(guildId).isInmMode(guildId)) {
            var inm = Commands.slash("inm", "INM補完").addOptions(new OptionData(OptionType.STRING, "search", "検索").setAutoComplete(true).setRequired(true)).setGuildOnly(true).setDefaultPermissions(defaultPermissions);
            cmds.add(inm);
        }

        if (!DiscordUtils.isNonAllowCookie(guildId) && getServerSaveData(guildId).isCookieMode(guildId)) {
            var cookie = Commands.slash("cookie", "クッキー☆補完").addOptions(new OptionData(OptionType.STRING, "search", "検索").setAutoComplete(true).setRequired(true)).setGuildOnly(true).setDefaultPermissions(defaultPermissions);
            cmds.add(cookie);
        }

        if (allJDA) {
            var jdas = JDAManager.getInstance().getAllJDA();
            synchronized (jdas) {
                for (JDA jda : jdas) {
                    var jg = jda.getGuildById(guildId);
                    if (jg != null)
                        jg.updateCommands().addCommands(cmds).queue();
                }
            }
        } else {
            guild.updateCommands().addCommands(cmds).queue();
        }
    }

    private static DefaultMemberPermissions getUseCommandPermission() {
        return DefaultMemberPermissions.enabledFor(Permission.VOICE_CONNECT, Permission.MESSAGE_SEND);
    }

    private static void updateGlobalCommands(JDA jda) {
        var defaultPermissions = getUseCommandPermission();

        var join = Commands.slash("join", "読み上げBOTをVCに呼び出す").addOptions(new OptionData(OptionType.CHANNEL, "channel", "チャンネル指定").setChannelTypes(ImmutableList.of(ChannelType.VOICE, ChannelType.STAGE))).setGuildOnly(true).setDefaultPermissions(defaultPermissions);

        var leave = Commands.slash("leave", "読み上げBOTをVCから切断").setGuildOnly(true).setDefaultPermissions(defaultPermissions);

        var reconnect = Commands.slash("reconnect", "読み上げBOTをVCに再接続").setGuildOnly(true).setDefaultPermissions(defaultPermissions);

        var voice = Commands.slash("voice", "読み上げ音声タイプ関係").setGuildOnly(true).setDefaultPermissions(defaultPermissions)
                .addSubcommands(new SubcommandData("check", "現在の読み上げ音声タイプを確認").addOptions(new OptionData(OptionType.USER, "user", "ユーザー指定")))
                .addSubcommands(new SubcommandData("show", "読み上げ音声タイプ一覧を表示"))
                .addSubcommands(new SubcommandData("change", "読み上げ音声タイプを変更").addOptions(new OptionData(OptionType.STRING, "voice_category", "読み上げ音声のカテゴリ").setAutoComplete(true).setRequired(true)).addOptions(new OptionData(OptionType.STRING, "voice_type", "読み上げる声タイプ").setAutoComplete(true).setRequired(true)).addOptions(new OptionData(OptionType.USER, "user", "ユーザー指定")));

        var deny = Commands.slash("deny", "読み上げ拒否関係").setGuildOnly(true).setDefaultPermissions(defaultPermissions)
                .addSubcommands(new SubcommandData("show", "読み上げ拒否一覧を表示"))
                .addSubcommands(new SubcommandData("add", "読み上げ拒否に追加").addOptions(new OptionData(OptionType.USER, "user", "ユーザー指定").setRequired(true)))
                .addSubcommands(new SubcommandData("remove", "読み上げ拒否を解除").addOptions(new OptionData(OptionType.USER, "user", "ユーザー指定").setRequired(true)));

        var config = Commands.slash("config", "読み上げ設定").setGuildOnly(true).setDefaultPermissions(defaultPermissions)
                .addSubcommands(new SubcommandData("need-join", "VCに参加時のみ読み上げ").addOptions(new OptionData(OptionType.BOOLEAN, "enable", "有効かどうか").setRequired(true)))
                .addSubcommands(new SubcommandData("overwrite-aloud", "読み上げの上書き").addOptions(new OptionData(OptionType.BOOLEAN, "enable", "有効かどうか").setRequired(true)))
                .addSubcommands(new SubcommandData("inm-mode", "淫夢モード").addOptions(new OptionData(OptionType.BOOLEAN, "enable", "有効かどうか").setRequired(true)))
                .addSubcommands(new SubcommandData("cookie-mode", "クッキー☆モード").addOptions(new OptionData(OptionType.BOOLEAN, "enable", "有効かどうか").setRequired(true)))
                .addSubcommands(new SubcommandData("join-say-name", "VCに参加時に名前を読み上げ").addOptions(new OptionData(OptionType.BOOLEAN, "enable", "有効かどうか").setRequired(true)))
                .addSubcommands(new SubcommandData("read-around-limit", "最大読み上げ文字数").addOptions(new OptionData(OptionType.INTEGER, "max-count", "最大文字数").setMinValue(1).setMaxValue(Integer.MAX_VALUE).setRequired(true)))
                .addSubcommands(new SubcommandData("non-reading-prefix", "先頭につけると読み上げなくなる文字").addOptions(new OptionData(OptionType.STRING, "prefix", "接頭辞").setRequired(true)))
                .addSubcommands(new SubcommandData("read-around-name-limit", "最大名前読み上げ文字数").addOptions(new OptionData(OptionType.INTEGER, "max-count", "最大文字数").setMinValue(1).setMaxValue(Integer.MAX_VALUE).setRequired(true)))
                .addSubcommands(new SubcommandData("show", "現在のコンフィグを表示"));

        var vnick = Commands.slash("vnick", "読み上げユーザ名変更").addOptions(new OptionData(OptionType.STRING, "name", "名前").setRequired(true)).addOptions(new OptionData(OptionType.USER, "user", "ユーザー指定")).setGuildOnly(true).setDefaultPermissions(defaultPermissions);

        var dict = Commands.slash("dict", "読み上げ辞書").setGuildOnly(true).setDefaultPermissions(defaultPermissions)
                .addSubcommands(new SubcommandData("show", "現在の読み上げ辞書を表示").addOptions(new OptionData(OptionType.STRING, "type", "辞書タイプ").addChoice("サーバー別辞書", "server").addChoice("グローバル辞書", "global")))
                .addSubcommands(new SubcommandData("add", "読み上げ辞書に単語を登録").addOption(OptionType.STRING, "word", "対象の単語", true).addOption(OptionType.STRING, "reading", "対象の読み", true))
                .addSubcommands(new SubcommandData("remove", "読み上げ辞書から単語を削除").addOption(OptionType.STRING, "word", "対象の単語", true, true))
                .addSubcommands(new SubcommandData("download", "現在の読み上げ辞書をダウンロード"))
                .addSubcommands(new SubcommandData("upload", "読み上げ辞書をアップロード").addOption(OptionType.ATTACHMENT, "file", "辞書ファイル", true).addOption(OptionType.BOOLEAN, "overwrite", "上書き", true));

        jda.updateCommands().addCommands(join, leave, reconnect, voice, deny, config, vnick, dict).queue();
    }

    public static void updatePresence() {
        long ct = TTSManager.getInstance().getTTSCount();

        String vstr = "v" + VERSION;

        JDAManager.getInstance().getAllJDA().forEach(jda -> {
            if (ct > 0) {
                jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.listening(vstr + " - " + ct + "個のチャンネルで読み上げ"));
            } else {
                jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.playing(vstr + " - " + "待機"));
            }
        });
    }

    public static ServerSaveData getServerSaveData(long guildId) {
        return ConfigAndSaveDataManager.getInstance().getServerSaveData(guildId);
    }

    public static SaveData getSaveData() {
        return ConfigAndSaveDataManager.getInstance().getSaveData();
    }

    public static Config getConfig() {
        return ConfigAndSaveDataManager.getInstance().getConfig();
    }

    public static Map<Long, ServerSaveData> getAllServerSaveData() {
        return ConfigAndSaveDataManager.getInstance().getAllServerSaveData();
    }

    public static List<JDA> getActiveJDAs(Guild guild) {
        return JDAManager.getInstance().getAllJDA().stream().filter(jda -> isConnectedTo(jda, guild)).toList();
    }

    public static boolean isConnectedTo(JDA jda, Guild guild) {
        return guild.getVoiceChannels().stream().anyMatch(vc -> vc.getMembers().stream().anyMatch(m -> m.getIdLong() == jda.getSelfUser().getIdLong()));
    }

    public static JDA getJDAByID(long userId) {
        return JDAManager.getInstance().getJDA(userId);
    }
}
