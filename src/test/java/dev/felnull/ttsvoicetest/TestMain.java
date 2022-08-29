package dev.felnull.ttsvoicetest;

import dev.felnull.ttsvoice.Main;
import dev.felnull.ttsvoice.discord.BotLocation;
import dev.felnull.ttsvoice.tts.TTSManager;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Random;
import java.util.UUID;

public class TestMain {
    public static void main(String[] args) throws Exception {
        args = ArrayUtils.add(args, "devtest");
        Main.main(args);
        //test();
    }

    private static void test() {
        Random random = new Random();
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep((long) (10000f * random.nextFloat()) + 10000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                TTSManager tm = TTSManager.getInstance();
                BotLocation bl = new BotLocation(1004106512152666113L, 600929948529590272L);
                var types = tm.getVoiceTypes(bl.botUserId(), bl.guildId());
                tm.setUserVoceTypes(bl.botUserId(), types.get(random.nextInt(types.size())));
                tm.sayChat(bl, 1004106512152666113L, UUID.randomUUID().toString(), 824546589250420766L, -1);
            }
        });
        t.start();
    }


}
