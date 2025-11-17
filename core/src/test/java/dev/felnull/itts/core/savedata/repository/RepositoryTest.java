package dev.felnull.itts.core.savedata.repository;

import dev.felnull.itts.core.dict.DictionaryUseEntry;
import dev.felnull.itts.core.savedata.MySQLTestOperation;
import dev.felnull.itts.core.savedata.dao.DAO;
import dev.felnull.itts.core.tts.TTSChannelPair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RepositoryTest extends RepoBaseTest {

    @Test
    @EnabledIfEnvironmentVariable(named = "I_TTS_MYSQL_TEST", matches = "ENABLE")
    void testMySQL() throws Exception {

        DAO dao1 = MySQLTestOperation.createDAO();
        dao1.init();
        try (Connection connection = dao1.getConnection()) {
            MySQLTestOperation.clearDataBase(connection);
        }
        dao1.dispose();

        DataRepository repo = DataRepository.create(MySQLTestOperation.createDAO());
        repo.init();

        ServerData serverData = repo.getServerData(114514);
        serverData.setIgnoreRegex("イキスギ");
        assertEquals("イキスギ", serverData.getIgnoreRegex());

        repo.dispose();

        DAO dao2 = MySQLTestOperation.createDAO();
        dao2.init();
        try (Connection connection = dao2.getConnection()) {
            MySQLTestOperation.clearDataBase(connection);
        }
        dao2.dispose();
    }

    @Test
    void testGetAllConnectedChannel() {
        DataRepository repo = createRepository();

        BotStateData botStateData1 = repo.getBotStateData(114L, 514L);
        BotStateData botStateData2 = repo.getBotStateData(364L, 514L);
        BotStateData botStateData3 = repo.getBotStateData(114L, 810L);
        BotStateData botStateData4 = repo.getBotStateData(110L, 810L);

        botStateData1.setConnectedChannelPair(new TTSChannelPair(10L, 20L));
        botStateData2.setConnectedChannelPair(new TTSChannelPair(30L, 40L));
        botStateData3.setConnectedChannelPair(new TTSChannelPair(50L, 60L));
        botStateData4.setConnectedChannelPair(null);

        Map<Long, TTSChannelPair> ret1 = repo.getAllConnectedChannel(514L);
        Map<Long, TTSChannelPair> ret2 = repo.getAllConnectedChannel(810L);

        assertEquals(2, ret1.size());
        assertEquals(new TTSChannelPair(10L, 20L), ret1.get(114L));
        assertEquals(new TTSChannelPair(30L, 40L), ret1.get(364L));

        assertEquals(1, ret2.size());
        assertEquals(new TTSChannelPair(50L, 60L), ret2.get(114L));

        repo.dispose();
    }

    @Test
    void testGetAllDenyUser() {
        DataRepository repo = createRepository();

        ServerUserData serverUserData1 = repo.getServerUserData(114L, 1919L);
        ServerUserData serverUserData2 = repo.getServerUserData(514L, 810L);
        ServerUserData serverUserData3 = repo.getServerUserData(114L, 364364L);
        ServerUserData serverUserData4 = repo.getServerUserData(114L, 110L);

        serverUserData1.setDeny(true);
        serverUserData2.setDeny(true);
        serverUserData3.setDeny(true);
        serverUserData4.setDeny(false);

        List<Long> ret = repo.getAllDenyUser(114L);
        assertEquals(2, ret.size());
        assertTrue(ret.contains(1919L));
        assertTrue(ret.contains(364364L));

        repo.dispose();
    }

    @Test
    void testGetAllDictionaryUseData() {
        DataRepository repo = createRepository();

        DictionaryUseData dictionaryUseData1 = repo.getDictionaryUseData(114L, "abbreviation");
        DictionaryUseData dictionaryUseData2 = repo.getDictionaryUseData(514L, "global");
        DictionaryUseData dictionaryUseData3 = repo.getDictionaryUseData(114L, "romaji");
        DictionaryUseData dictionaryUseData4 = repo.getDictionaryUseData(114L, "server");

        dictionaryUseData1.setEnable(true);
        dictionaryUseData1.setPriority(5);

        dictionaryUseData2.setEnable(true);
        dictionaryUseData2.setPriority(2);

        dictionaryUseData3.setEnable(false);
        dictionaryUseData3.setPriority(6);

        dictionaryUseData4.setEnable(null);
        dictionaryUseData4.setPriority(null);

        List<DictionaryUseEntry> ret = repo.getAllDictionaryUseData(114L);
        assertEquals(3, ret.size());
        assertTrue(ret.contains(new DictionaryUseEntry("abbreviation", true, 5)));
        assertTrue(ret.contains(new DictionaryUseEntry("romaji", false, 6)));
        assertTrue(ret.contains(new DictionaryUseEntry("server", null, null)));

        repo.dispose();
    }

}
