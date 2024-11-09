package dev.felnull.itts.core.savedata.repository;

import dev.felnull.itts.core.savedata.dao.DAO;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class RepoErrorListenerTest extends RepoBaseTest {

    @Test
    void testRepoGetDataAndAddRemove() throws Exception {
        AtomicBoolean errorCheck = new AtomicBoolean();
        RepoErrorListener errorListener = () -> errorCheck.set(true);

        DAO spyDao = Mockito.spy(createDAO());

        DAO.BotStateDataTable spyStateDataTable = Mockito.spy(spyDao.botStateDataTable());
        Mockito.doThrow(new IllegalStateException("ｱｲｷ")).when(spyStateDataTable).selectAllConnectedChannelPairByBotKeyId(Mockito.any(), Mockito.anyInt());
        Mockito.when(spyDao.botStateDataTable()).thenReturn(spyStateDataTable);

        DAO.ServerUserDataTable serverUserDataTable = Mockito.spy(spyDao.serverUserDataTable());
        Mockito.doThrow(new IllegalStateException("ｱｲｷ")).when(serverUserDataTable).selectAllDenyUser(Mockito.any(), Mockito.anyInt());
        Mockito.when(spyDao.serverUserDataTable()).thenReturn(serverUserDataTable);

        DAO.DictionaryUseDataTable dictionaryUseDataTable = Mockito.spy(spyDao.dictionaryUseDataTable());
        Mockito.doThrow(new IllegalStateException("ｱｲｷ")).when(dictionaryUseDataTable).selectAll(Mockito.any(), Mockito.anyInt());
        Mockito.when(spyDao.dictionaryUseDataTable()).thenReturn(dictionaryUseDataTable);

        DataRepository repository = DataRepository.create(spyDao);
        repository.init();
        repository.addErrorListener(errorListener);

        assertFalse(errorCheck.get());
        assertThrows(RuntimeException.class, () -> repository.getAllConnectedChannel(514L));
        assertTrue(errorCheck.getAndSet(false));

        assertFalse(errorCheck.get());
        assertThrows(RuntimeException.class, () -> repository.getAllDenyUser(514L));
        assertTrue(errorCheck.getAndSet(false));

        assertFalse(errorCheck.get());
        assertThrows(RuntimeException.class, () -> repository.getAllDictionaryUseData(514L));
        assertTrue(errorCheck.getAndSet(false));

        repository.removeErrorListener(errorListener);

        assertFalse(errorCheck.get());
        assertThrows(RuntimeException.class, () -> repository.getAllConnectedChannel(514L));
        assertFalse(errorCheck.get());

        repository.dispose();
    }

    @Test
    void testRecordDataInit() throws Exception {
        AtomicBoolean errorCheck = new AtomicBoolean();
        RepoErrorListener errorListener = () -> errorCheck.set(true);

        DAO spyDao = Mockito.spy(createDAO());

        DAO.ServerDataTable spyServerDataTable = Mockito.spy(spyDao.serverDataTable());
        Mockito.doThrow(new IllegalStateException("ｱｲｷ")).when(spyServerDataTable).insertRecordIfNotExists(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.when(spyDao.serverDataTable()).thenReturn(spyServerDataTable);

        DataRepository repository = DataRepository.create(spyDao);
        repository.init();
        repository.addErrorListener(errorListener);

        assertFalse(errorCheck.get());
        assertThrows(RuntimeException.class, () -> repository.getServerData(514L));
        assertTrue(errorCheck.get());

        repository.dispose();
    }

    @Test
    void testAfterDispose() {
        AtomicBoolean errorCheck = new AtomicBoolean();
        RepoErrorListener errorListener = () -> errorCheck.set(true);

        DataRepository repository = createRepository();
        repository.addErrorListener(errorListener);
        repository.getServerData(10L).getIgnoreRegex();
        repository.dispose();

        assertFalse(errorCheck.get());
        assertThrows(RuntimeException.class, () -> repository.getServerData(10L).getIgnoreRegex());
        assertFalse(errorCheck.get());
    }

    @Test
    void testSaveDataBaseProc() throws Exception {
        AtomicBoolean errorCheck = new AtomicBoolean();
        RepoErrorListener errorListener = () -> errorCheck.set(true);

        DAO spyDao = Mockito.spy(createDAO());

        DAO.ServerDataTable spyServerDataTable = Mockito.spy(spyDao.serverDataTable());
        Mockito.doThrow(new IllegalStateException("ｱｲｷ")).when(spyServerDataTable).selectReadLimit(Mockito.any(), Mockito.anyInt());
        Mockito.doThrow(new IllegalStateException("ｱｲｷ")).when(spyServerDataTable).updateNeedJoin(Mockito.any(), Mockito.anyInt(), Mockito.anyBoolean());
        Mockito.when(spyDao.serverDataTable()).thenReturn(spyServerDataTable);

        DataRepository repository = DataRepository.create(spyDao);
        repository.init();
        repository.addErrorListener(errorListener);

        assertFalse(errorCheck.get());
        assertThrows(RuntimeException.class, () -> repository.getServerData(514L).getReadLimit());
        assertTrue(errorCheck.getAndSet(false));

        assertFalse(errorCheck.get());
        assertThrows(RuntimeException.class, () -> repository.getServerData(364L).setNeedJoin(true));
        assertTrue(errorCheck.get());

        repository.dispose();
    }
}
