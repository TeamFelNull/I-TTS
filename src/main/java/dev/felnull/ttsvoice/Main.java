package dev.felnull.ttsvoice;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dev.felnull.ttsvoice.tts.TTSListener;
import dev.felnull.ttsvoice.tts.TTSManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Timer;
import java.util.TimerTask;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger(Main.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File SAVE_FILE = new File("./save.json");
    public static final SaveData SAVE_DATA = new SaveData();
    public static Config CONFIG;
    public static JDA JDA;

    public static void main(String[] args) throws Exception {
        LOGGER.info("Start!");

        var configFile = new File("./config.json");

        if (configFile.exists()) {
            try (Reader reader = new BufferedReader(new FileReader(configFile))) {
                CONFIG = Config.of(GSON.fromJson(reader, JsonObject.class));
            }
            LOGGER.info("Config file was loaded");
        } else {
            LOGGER.warn("No config file, generate default config");
            CONFIG = Config.createDefault();
            try (Writer writer = new BufferedWriter(new FileWriter(configFile))) {
                GSON.toJson(CONFIG.toJson(), writer);
            }
            return;
        }

        try {
            CONFIG.check();
        } catch (Exception ex) {
            LOGGER.error("Config is incorrect", ex);
            return;
        }

        LOGGER.info("Completed config check");

        if (SAVE_FILE.exists()) {
            JsonObject jo;
            try (Reader reader = new InputStreamReader(new BufferedInputStream(new FileInputStream(SAVE_FILE)))) {
                jo = GSON.fromJson(reader, JsonObject.class);
            }
            SAVE_DATA.load(jo);
            LOGGER.info("Completed load data");
        }

        Timer timer = new Timer();
        TimerTask saveTask = new TimerTask() {
            public void run() {
                if (SAVE_DATA.isDirty()) {
                    var jo = SAVE_DATA.save();
                    try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(SAVE_FILE)))) {
                        GSON.toJson(jo, writer);
                        LOGGER.info("Completed to save data");
                    } catch (Exception ex) {
                        LOGGER.error("Failed to save data", ex);
                    }
                    SAVE_DATA.setDirty(false);
                }
            }
        };
        timer.scheduleAtFixedRate(saveTask, 0, 30 * 1000);

        TTSManager.getInstance().init();

        JDA = JDABuilder.createDefault(CONFIG.botToken()).addEventListeners(new TTSListener()).build();
        JDA.getPresence().setPresence(OnlineStatus.ONLINE, Activity.watching("Ikisugi TTS"));

        var join = Commands.slash("join", "読み上げBOTをVCに呼び出す").addOptions(new OptionData(OptionType.CHANNEL, "channel", "チャンネル指定").setChannelTypes(ImmutableList.of(ChannelType.VOICE, ChannelType.STAGE)));
        var leave = Commands.slash("leave", "読み上げBOTをVCから切断");
        var reconnect = Commands.slash("reconnect", "読み上げBOTをVCに再接続");
        var voice = Commands.slash("voice", "読み上げ音声タイプ関係").addSubcommands(new SubcommandData("list", "読み上げ音声タイプ一覧を表示")).addSubcommands(new SubcommandData("change", "読み上げ音声タイプを変更").addOptions(new OptionData(OptionType.STRING, "voice_type", "読み上げる声タイプ").setAutoComplete(true).setRequired(true)).addOptions(new OptionData(OptionType.USER, "user", "ユーザー指定")));

        JDA.updateCommands().addCommands(join, leave, reconnect, voice).queue();
    }
}
