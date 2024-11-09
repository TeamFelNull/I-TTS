package dev.felnull.itts.core.savedata.dao;

import dev.felnull.itts.core.dict.DictionaryUseEntry;
import dev.felnull.itts.core.dict.ReplaceType;
import dev.felnull.itts.core.discord.AutoDisconnectMode;
import dev.felnull.itts.core.savedata.AbstractSaveDataTest;
import dev.felnull.itts.core.tts.TTSChannelPair;
import dev.felnull.itts.core.util.TestUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public abstract class DAOAccessTest extends AbstractSaveDataTest {
    protected static DAO dao;

    @AfterAll
    static void afterAll() throws IOException {
        dao.dispose();
        dao = null;
    }

    // Keys

    @Test
    void testServerKeyTable() throws Exception {
        try (Connection connection = dao.getConnection()) {
            Set<Integer> ids = new HashSet<>();
            TestUtils.testForEach(discordServerIdsData(), it -> keyTableTest(connection, ids, dao.serverKeyTable(), it));
        }
    }

    @Test
    void testUserKeyTable() throws Exception {
        try (Connection connection = dao.getConnection()) {
            Set<Integer> ids = new HashSet<>();
            TestUtils.testForEach(discordUserIdsData(), it -> keyTableTest(connection, ids, dao.userKeyTable(), it));
        }
    }

    @Test
    void testBotKeyTable() throws Exception {
        try (Connection connection = dao.getConnection()) {
            Set<Integer> ids = new HashSet<>();
            TestUtils.testForEach(discordBotIdsData(), it -> keyTableTest(connection, ids, dao.botKeyTable(), it));
        }
    }

    @Test
    void testChannelKeyTable() throws Exception {
        try (Connection connection = dao.getConnection()) {
            Set<Integer> ids = new HashSet<>();
            TestUtils.testForEach(discordChannelIdsData(), it -> keyTableTest(connection, ids, dao.channelKeyTable(), it));
        }
    }

    @Test
    void testDictionaryKeyTable() throws Exception {
        try (Connection connection = dao.getConnection()) {
            Set<Integer> ids = new HashSet<>();
            TestUtils.testForEach(dictionaryIdsData(), it -> keyTableTest(connection, ids, dao.dictionaryKeyTable(), it));
        }
    }

    @Test
    void testDictionaryReplaceTypeKeyTable() throws Exception {
        try (Connection connection = dao.getConnection()) {
            Set<Integer> ids = new HashSet<>();
            TestUtils.testForEach(dictionaryReplaceTypesData().map(ReplaceType::getName), it -> keyTableTest(connection, ids, dao.dictionaryReplaceTypeKeyTable(), it));
        }
    }

    @Test
    void testAutoDisconnectModeKeyTable() throws Exception {
        try (Connection connection = dao.getConnection()) {
            Set<Integer> ids = new HashSet<>();
            TestUtils.testForEach(autoDisconnectModesData().map(AutoDisconnectMode::getName), it -> keyTableTest(connection, ids, dao.autoDisconnectModeKeyTable(), it));
        }
    }

    @Test
    void testVoiceTypeKeyTable() throws Exception {
        try (Connection connection = dao.getConnection()) {
            Set<Integer> ids = new HashSet<>();
            TestUtils.testForEach(voiceTypeNamesData(), it -> keyTableTest(connection, ids, dao.voiceTypeKeyTable(), it));
        }
    }

    private <T> void keyTableTest(Connection connection, Set<Integer> ids, DAO.KeyTable<T> table, T key) throws SQLException {
        // テーブル作成
        table.createTableIfNotExists(connection);

        // 作成済みの場合にエラーが出ないか確認
        table.createTableIfNotExists(connection);

        // エントリが存在しないことを確認
        assertTrue(table.selectId(connection, key).isEmpty());
        assertTrue(table.selectKey(connection, ids.isEmpty() ? 1 : (Collections.max(ids) + 1)).isEmpty());

        // エントリ追加後にIDを取得できる確認
        table.insertKeyIfNotExists(connection, key);
        OptionalInt id = table.selectId(connection, key);
        assertTrue(id.isPresent());

        // エントリ追加済みの場合は同じIDを取得できるか確認
        table.insertKeyIfNotExists(connection, key);
        assertEquals(id.getAsInt(), table.selectId(connection, key).getAsInt());

        // IDからキーを取得できるか確認
        Optional<T> selectKeyRet = table.selectKey(connection, id.getAsInt());
        assertTrue(selectKeyRet.isPresent());
        assertEquals(key, selectKeyRet.get());

        ids.add(id.getAsInt());
    }

    // ServerDataTable

    @Test
    void testServerDataTableInsert() throws Exception {
        try (Connection connection = dao.getConnection()) {
            serverDataTableTestCreateTable(connection);

            TestUtils.testForEach(discordServerIdsData(), serverId -> {
                int serverKeyId = insertAndSelectKeyId(connection, dao.serverKeyTable(), serverId);
                ServerKey serverKey = new ServerKey(serverKeyId);
                ServerDataRecord[] firstRecord = new ServerDataRecord[1];

                TestUtils.forEachIndexed(serverDataTableTestRecordData(connection), (idx, record) -> {
                    dao.serverDataTable().insertRecordIfNotExists(connection, serverKey, record);
                    if (idx == 0) {
                        // 追加したレコードと取得できるレコードが一致する確認
                        serverDataTableTestSelectCheck(connection, serverKey, record);
                        firstRecord[0] = record;
                    } else {
                        // 既にレコードが存在している状態でInsertした場合に、上書きされないことを確認
                        serverDataTableTestSelectCheck(connection, serverKey, firstRecord[0]);
                    }
                });
            });
        }
    }

    @Test
    void testServerDataTableUpdate() throws Exception {
        try (Connection connection = dao.getConnection()) {
            serverDataTableTestCreateTable(connection);

            int autoDisconnectModeKeyId = insertAndSelectKeyId(connection, dao.autoDisconnectModeKeyTable(), AutoDisconnectMode.ON.getName());
            ServerDataRecord initRecord = new ServerDataRecord(null, null, false, false, false, 0, 0, autoDisconnectModeKeyId);

            TestUtils.testForEach(discordServerIdsData(), serverId -> {
                int serverKeyId = insertAndSelectKeyId(connection, dao.serverKeyTable(), serverId);
                ServerKey serverKey = new ServerKey(serverKeyId);

                dao.serverDataTable().insertRecordIfNotExists(connection, serverKey, initRecord);

                Optional<IdRecordPair<ServerDataRecord>> preIdAndRecord = dao.serverDataTable().selectRecordByKey(connection, serverKey);
                int tableId = preIdAndRecord.orElseThrow().getId();

                TestUtils.testForEach(serverDataTableTestRecordData(connection), record -> {
                    // 単体でレコードの値を更新
                    dao.serverDataTable().updateDefaultVoiceType(connection, tableId, record.defaultVoiceTypeKeyId());
                    dao.serverDataTable().updateIgnoreRegex(connection, tableId, record.ignoreRegex());
                    dao.serverDataTable().updateNeedJoin(connection, tableId, record.needJoin());
                    dao.serverDataTable().updateOverwriteAloud(connection, tableId, record.overwriteAloud());
                    dao.serverDataTable().updateNotifyMove(connection, tableId, record.notifyMove());
                    dao.serverDataTable().updateReadLimit(connection, tableId, record.readLimit());
                    dao.serverDataTable().updateNameReadLimit(connection, tableId, record.nameReadLimit());
                    dao.serverDataTable().updateAutoDisconnectMode(connection, tableId, record.autoDisconnectModeKeyId());

                    serverDataTableTestSelectCheck(connection, serverKey, record);
                });
            });
        }
    }

    @Test
    void testServerDataTableNotExist() throws Exception {
        try (Connection connection = dao.getConnection()) {
            serverDataTableTestCreateTable(connection);

            int autoDisconnectModeKeyId = insertAndSelectKeyId(connection, dao.autoDisconnectModeKeyTable(), AutoDisconnectMode.ON.getName());
            ServerDataRecord record = new ServerDataRecord(null, null, false, false, false, 0, 0, autoDisconnectModeKeyId);

            int serverKeyId = insertAndSelectKeyId(connection, dao.serverKeyTable(), 114514L);
            ServerKey serverKey = new ServerKey(serverKeyId);

            // 存在しないキーのレコードを取得
            assertTrue(dao.serverDataTable().selectRecordByKey(connection, serverKey).isEmpty());

            int recordId = 3;

            // 存在しないレコードIDで取得
            assertTrue(dao.serverDataTable().selectRecordById(connection, recordId).isEmpty());

            // 存在しないレコードIDで取得(単体)
            assertThrows(IllegalStateException.class, () -> dao.serverDataTable().selectDefaultVoiceType(connection, recordId));
            assertThrows(IllegalStateException.class, () -> dao.serverDataTable().selectIgnoreRegex(connection, recordId));
            assertThrows(IllegalStateException.class, () -> dao.serverDataTable().selectNeedJoin(connection, recordId));
            assertThrows(IllegalStateException.class, () -> dao.serverDataTable().selectOverwriteAloud(connection, recordId));
            assertThrows(IllegalStateException.class, () -> dao.serverDataTable().selectNotifyMove(connection, recordId));
            assertThrows(IllegalStateException.class, () -> dao.serverDataTable().selectReadLimit(connection, recordId));
            assertThrows(IllegalStateException.class, () -> dao.serverDataTable().selectNameReadLimit(connection, recordId));
            assertThrows(IllegalStateException.class, () -> dao.serverDataTable().selectAutoDisconnectMode(connection, recordId));

            // 存在しないレコードIDで更新
            assertThrows(IllegalStateException.class, () -> dao.serverDataTable().updateDefaultVoiceType(connection, recordId, record.defaultVoiceTypeKeyId()));
            assertThrows(IllegalStateException.class, () -> dao.serverDataTable().updateIgnoreRegex(connection, recordId, record.ignoreRegex()));
            assertThrows(IllegalStateException.class, () -> dao.serverDataTable().updateNeedJoin(connection, recordId, record.needJoin()));
            assertThrows(IllegalStateException.class, () -> dao.serverDataTable().updateOverwriteAloud(connection, recordId, record.overwriteAloud()));
            assertThrows(IllegalStateException.class, () -> dao.serverDataTable().updateNotifyMove(connection, recordId, record.notifyMove()));
            assertThrows(IllegalStateException.class, () -> dao.serverDataTable().updateReadLimit(connection, recordId, record.readLimit()));
            assertThrows(IllegalStateException.class, () -> dao.serverDataTable().updateNameReadLimit(connection, recordId, record.nameReadLimit()));
            assertThrows(IllegalStateException.class, () -> dao.serverDataTable().updateAutoDisconnectMode(connection, recordId, record.autoDisconnectModeKeyId()));
        }
    }

    private void serverDataTableTestCreateTable(Connection connection) throws Exception {
        // テストで必要なテーブルを作成
        dao.serverKeyTable().createTableIfNotExists(connection);
        dao.voiceTypeKeyTable().createTableIfNotExists(connection);
        dao.autoDisconnectModeKeyTable().createTableIfNotExists(connection);

        dao.serverDataTable().createTableIfNotExists(connection);

        // 作成済みの場合にエラーが出ないか確認
        dao.serverDataTable().createTableIfNotExists(connection);
    }

    private Stream<ServerDataRecord> serverDataTableTestRecordData(Connection connection) {
        // テストで使うデータのストリームを作成
        return createTestDataStream(serverDataRecordData(), voiceTypeNamesNullableData(), autoDisconnectModesData().map(AutoDisconnectMode::getName))
                .map(data -> {
                    try {
                        Integer voiceTypeKeyId = insertAndSelectKeyId(connection, dao.voiceTypeKeyTable(), data.getMiddle());
                        int autoDisconnectModeKeyId = insertAndSelectKeyId(connection, dao.autoDisconnectModeKeyTable(), data.getRight());
                        ServerDataRecord serverData = data.getLeft();

                        return new ServerDataRecord(
                                voiceTypeKeyId,
                                serverData.ignoreRegex(),
                                serverData.needJoin(),
                                serverData.overwriteAloud(),
                                serverData.notifyMove(),
                                serverData.readLimit(),
                                serverData.nameReadLimit(),
                                autoDisconnectModeKeyId
                        );
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private void serverDataTableTestSelectCheck(Connection connection, ServerKey key, ServerDataRecord expectedRecord) throws Exception {
        /* 取得した値が期待するレコードと一致するか確認 */

        // キーから取得して確認
        Optional<IdRecordPair<ServerDataRecord>> retIdAndRecord = dao.serverDataTable().selectRecordByKey(connection, key);
        assertTrue(retIdAndRecord.isPresent());
        assertEquals(expectedRecord, retIdAndRecord.get().getRecord());

        int tableId = retIdAndRecord.get().getId();

        // テーブルIDから取得して確認
        Optional<ServerDataRecord> retRecordById = dao.serverDataTable().selectRecordById(connection, tableId);
        assertTrue(retRecordById.isPresent());
        assertEquals(expectedRecord, retRecordById.get());

        // レコードの値を単体で取得して確認
        assertEquals(TestUtils.getOptionalIntByInteger(expectedRecord.defaultVoiceTypeKeyId()), dao.serverDataTable().selectDefaultVoiceType(connection, tableId));
        assertEquals(expectedRecord.ignoreRegex(), dao.serverDataTable().selectIgnoreRegex(connection, tableId).orElse(null));
        assertEquals(expectedRecord.needJoin(), dao.serverDataTable().selectNeedJoin(connection, tableId));
        assertEquals(expectedRecord.overwriteAloud(), dao.serverDataTable().selectOverwriteAloud(connection, tableId));
        assertEquals(expectedRecord.notifyMove(), dao.serverDataTable().selectNotifyMove(connection, tableId));
        assertEquals(expectedRecord.readLimit(), dao.serverDataTable().selectReadLimit(connection, tableId));
        assertEquals(expectedRecord.nameReadLimit(), dao.serverDataTable().selectNameReadLimit(connection, tableId));
        assertEquals(expectedRecord.autoDisconnectModeKeyId(), dao.serverDataTable().selectAutoDisconnectMode(connection, tableId));
    }

    // ServerUserDataTable

    @Test
    void testServerUserDataTableInsert() throws Exception {
        try (Connection connection = dao.getConnection()) {
            serverUserDataTableTestCreateTable(connection);

            TestUtils.testForEach(createTestDataStream(discordServerIdsData(), discordUserIdsData()), serverIdAndUserId -> {
                int serverKeyId = insertAndSelectKeyId(connection, dao.serverKeyTable(), serverIdAndUserId.getLeft());
                int userKeyId = insertAndSelectKeyId(connection, dao.userKeyTable(), serverIdAndUserId.getRight());

                ServerUserKey serverUserKey = new ServerUserKey(serverKeyId, userKeyId);
                ServerUserDataRecord[] firstRecord = new ServerUserDataRecord[1];

                TestUtils.forEachIndexed(serverUserDataTableTestRecordData(connection), (idx, record) -> {
                    dao.serverUserDataTable().insertRecordIfNotExists(connection, serverUserKey, record);
                    if (idx == 0) {
                        // 追加したレコードと取得できるレコードが一致する確認
                        serverUserDataTableTestSelectCheck(connection, serverUserKey, record);
                        firstRecord[0] = record;
                    } else {
                        // 既にレコードが存在している状態でInsertした場合に、上書きされないことを確認
                        serverUserDataTableTestSelectCheck(connection, serverUserKey, firstRecord[0]);
                    }
                });
            });
        }
    }

    @Test
    void testServerUserDataTableUpdate() throws Exception {
        try (Connection connection = dao.getConnection()) {
            serverUserDataTableTestCreateTable(connection);

            ServerUserDataRecord initRecord = new ServerUserDataRecord(null, false, null);

            TestUtils.testForEach(createTestDataStream(discordServerIdsData(), discordUserIdsData()), serverIdAndUserId -> {
                int serverKeyId = insertAndSelectKeyId(connection, dao.serverKeyTable(), serverIdAndUserId.getLeft());
                int userKeyId = insertAndSelectKeyId(connection, dao.userKeyTable(), serverIdAndUserId.getRight());
                ServerUserKey serverUserKey = new ServerUserKey(serverKeyId, userKeyId);

                dao.serverUserDataTable().insertRecordIfNotExists(connection, serverUserKey, initRecord);

                Optional<IdRecordPair<ServerUserDataRecord>> preIdAndRecord = dao.serverUserDataTable().selectRecordByKey(connection, serverUserKey);
                int tableId = preIdAndRecord.orElseThrow().getId();

                TestUtils.testForEach(serverUserDataTableTestRecordData(connection), record -> {
                    // 単体でレコードの値を更新
                    dao.serverUserDataTable().updateVoiceType(connection, tableId, record.voiceTypeKeyId());
                    dao.serverUserDataTable().updateDeny(connection, tableId, record.deny());
                    dao.serverUserDataTable().updateNickName(connection, tableId, record.nickName());

                    serverUserDataTableTestSelectCheck(connection, serverUserKey, record);
                });
            });
        }
    }

    @Test
    void testServerUserDataTableNotExist() throws Exception {
        try (Connection connection = dao.getConnection()) {
            serverUserDataTableTestCreateTable(connection);

            ServerUserDataRecord record = new ServerUserDataRecord(null, false, null);

            int serverKeyId = insertAndSelectKeyId(connection, dao.serverKeyTable(), 114514L);
            int userKeyId = insertAndSelectKeyId(connection, dao.userKeyTable(), 364364L);
            ServerUserKey serverUserKey = new ServerUserKey(serverKeyId, userKeyId);

            // 存在しないキーからレコードを取得
            assertTrue(dao.serverUserDataTable().selectRecordByKey(connection, serverUserKey).isEmpty());

            int recordId = 3;

            // 存在しないレコードIDで取得
            assertTrue(dao.serverUserDataTable().selectRecordById(connection, recordId).isEmpty());

            // 存在しないレコードIDで取得(単体)
            assertThrows(IllegalStateException.class, () -> dao.serverUserDataTable().selectVoiceType(connection, recordId));
            assertThrows(IllegalStateException.class, () -> dao.serverUserDataTable().selectDeny(connection, recordId));
            assertThrows(IllegalStateException.class, () -> dao.serverUserDataTable().selectNickName(connection, recordId));

            // 存在しないレコードIDで更新
            assertThrows(IllegalStateException.class, () -> dao.serverUserDataTable().updateVoiceType(connection, recordId, record.voiceTypeKeyId()));
            assertThrows(IllegalStateException.class, () -> dao.serverUserDataTable().updateDeny(connection, recordId, record.deny()));
            assertThrows(IllegalStateException.class, () -> dao.serverUserDataTable().updateNickName(connection, recordId, record.nickName()));
        }
    }

    @Test
    void testServerUserDataTableAllDenyUser() throws Exception {
        try (Connection connection = dao.getConnection()) {
            serverUserDataTableTestCreateTable(connection);

            int voiceTypeKeyId = insertAndSelectKeyId(connection, dao.voiceTypeKeyTable(), "kbtit");
            int serverKeyId = insertAndSelectKeyId(connection, dao.serverKeyTable(), 114514L);

            List<Long> expectedDenyUsers = new ArrayList<>();

            TestUtils.testForEach(createTestDataStream(discordUserIdsData().boxed(), serverUserDataRecordData()), data -> {
                int userKeyId = insertAndSelectKeyId(connection, dao.userKeyTable(), data.getLeft());
                ServerUserKey serverUserKey = new ServerUserKey(serverKeyId, userKeyId);
                ServerUserDataRecord dataRecord = data.getRight();
                dao.serverUserDataTable().insertRecordIfNotExists(connection, serverUserKey, new ServerUserDataRecord(voiceTypeKeyId, dataRecord.deny(), dataRecord.nickName()));

                if (dataRecord.deny()) {
                    expectedDenyUsers.add(dao.userKeyTable().selectKey(connection, userKeyId).orElseThrow());
                }
            });

            // 拒否ユーザーが一致するか確認
            List<Long> denyUsers = dao.serverUserDataTable().selectAllDenyUser(connection, serverKeyId);
            assertTrue(!denyUsers.isEmpty() && CollectionUtils.isEqualCollection(expectedDenyUsers, denyUsers));
        }
    }

    private void serverUserDataTableTestCreateTable(Connection connection) throws Exception {
        // テストで必要なテーブルを作成
        dao.serverKeyTable().createTableIfNotExists(connection);
        dao.userKeyTable().createTableIfNotExists(connection);
        dao.voiceTypeKeyTable().createTableIfNotExists(connection);

        dao.serverUserDataTable().createTableIfNotExists(connection);

        // 作成済みの場合にエラーが出ないか確認
        dao.serverUserDataTable().createTableIfNotExists(connection);
    }

    private Stream<ServerUserDataRecord> serverUserDataTableTestRecordData(Connection connection) {
        return createTestDataStream(serverUserDataRecordData(), voiceTypeNamesNullableData())
                .map(data -> {
                    try {
                        Integer voiceTypeKeyId = insertAndSelectKeyId(connection, dao.voiceTypeKeyTable(), data.getRight());
                        ServerUserDataRecord serverUserData = data.getLeft();

                        return new ServerUserDataRecord(voiceTypeKeyId, serverUserData.deny(), serverUserData.nickName());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private void serverUserDataTableTestSelectCheck(Connection connection, ServerUserKey key, ServerUserDataRecord expectedRecord) throws Exception {
        /* 取得した値が期待するレコードと一致するか確認 */

        // キーから取得して確認
        Optional<IdRecordPair<ServerUserDataRecord>> retIdAndRecord = dao.serverUserDataTable().selectRecordByKey(connection, key);
        assertTrue(retIdAndRecord.isPresent());
        assertEquals(expectedRecord, retIdAndRecord.get().getRecord());

        int tableId = retIdAndRecord.get().getId();

        // テーブルIDから取得して確認
        Optional<ServerUserDataRecord> retRecordById = dao.serverUserDataTable().selectRecordById(connection, tableId);
        assertTrue(retRecordById.isPresent());
        assertEquals(expectedRecord, retRecordById.get());

        // レコードの値を単体で取得して確認
        assertEquals(TestUtils.getOptionalIntByInteger(expectedRecord.voiceTypeKeyId()), dao.serverUserDataTable().selectVoiceType(connection, tableId));
        assertEquals(expectedRecord.deny(), dao.serverUserDataTable().selectDeny(connection, tableId));
        assertEquals(expectedRecord.nickName(), dao.serverUserDataTable().selectNickName(connection, tableId).orElse(null));
    }

    // DictionaryUseDataTable

    @Test
    void testDictionaryUseDataTableInsert() throws Exception {
        try (Connection connection = dao.getConnection()) {
            dictionaryUseDataTableTestCreateTable(connection);

            TestUtils.testForEach(createTestDataStream(discordServerIdsData().boxed(), dictionaryIdsData()), serverIdAndDictName -> {
                int serverKeyId = insertAndSelectKeyId(connection, dao.serverKeyTable(), serverIdAndDictName.getLeft());
                int dictKeyId = insertAndSelectKeyId(connection, dao.dictionaryKeyTable(), serverIdAndDictName.getRight());

                ServerDictionaryKey serverDictionaryKey = new ServerDictionaryKey(serverKeyId, dictKeyId);
                DictionaryUseDataRecord[] firstRecord = new DictionaryUseDataRecord[1];

                TestUtils.forEachIndexed(dictionaryUseDataTableTestRecordData(), (idx, record) -> {
                    dao.dictionaryUseDataTable().insertRecordIfNotExists(connection, serverDictionaryKey, record);
                    if (idx == 0) {
                        // 追加したレコードと取得できるレコードが一致する確認
                        dictionaryUseDataTableTestSelectCheck(connection, serverDictionaryKey, record);
                        firstRecord[0] = record;
                    } else {
                        // 既にレコードが存在している状態でInsertした場合に、上書きされないことを確認
                        dictionaryUseDataTableTestSelectCheck(connection, serverDictionaryKey, firstRecord[0]);
                    }
                });
            });
        }
    }

    @Test
    void testDictionaryUseDataTableUpdate() throws Exception {
        try (Connection connection = dao.getConnection()) {
            dictionaryUseDataTableTestCreateTable(connection);

            DictionaryUseDataRecord initRecord = new DictionaryUseDataRecord(null, 0);

            TestUtils.testForEach(createTestDataStream(discordServerIdsData().boxed(), dictionaryIdsData()), serverIdAndDictName -> {
                int serverKeyId = insertAndSelectKeyId(connection, dao.serverKeyTable(), serverIdAndDictName.getLeft());
                int dictId = insertAndSelectKeyId(connection, dao.dictionaryKeyTable(), serverIdAndDictName.getRight());
                ServerDictionaryKey serverDictionaryKey = new ServerDictionaryKey(serverKeyId, dictId);

                dao.dictionaryUseDataTable().insertRecordIfNotExists(connection, serverDictionaryKey, initRecord);

                Optional<IdRecordPair<DictionaryUseDataRecord>> preIdAndRecord = dao.dictionaryUseDataTable().selectRecordByKey(connection, serverDictionaryKey);
                int tableId = preIdAndRecord.orElseThrow().getId();

                TestUtils.testForEach(dictionaryUseDataTableTestRecordData(), record -> {
                    // 単体でレコードの値を更新
                    dao.dictionaryUseDataTable().updateEnable(connection, tableId, record.enable());
                    dao.dictionaryUseDataTable().updatePriority(connection, tableId, record.priority());

                    dictionaryUseDataTableTestSelectCheck(connection, serverDictionaryKey, record);
                });
            });
        }
    }

    @Test
    void testDictionaryUseDataTableNotExist() throws Exception {
        try (Connection connection = dao.getConnection()) {
            dictionaryUseDataTableTestCreateTable(connection);

            DictionaryUseDataRecord record = new DictionaryUseDataRecord(null, 0);

            int serverKeyId = insertAndSelectKeyId(connection, dao.serverKeyTable(), 114514L);
            int dictKeyId = insertAndSelectKeyId(connection, dao.dictionaryKeyTable(), "abbreviation");
            ServerDictionaryKey serverDictionaryKey = new ServerDictionaryKey(serverKeyId, dictKeyId);

            // 存在しないキーからレコードを取得
            assertTrue(dao.dictionaryUseDataTable().selectRecordByKey(connection, serverDictionaryKey).isEmpty());

            int recordId = 3;

            // 存在しないレコードIDで取得
            assertTrue(dao.dictionaryUseDataTable().selectRecordById(connection, recordId).isEmpty());

            // 存在しないレコードIDで取得(単体)
            assertThrows(IllegalStateException.class, () -> dao.dictionaryUseDataTable().selectEnable(connection, recordId));
            assertThrows(IllegalStateException.class, () -> dao.dictionaryUseDataTable().selectPriority(connection, recordId));

            // 存在しないレコードIDで更新
            assertThrows(IllegalStateException.class, () -> dao.dictionaryUseDataTable().updateEnable(connection, recordId, record.enable()));
            assertThrows(IllegalStateException.class, () -> dao.dictionaryUseDataTable().updatePriority(connection, recordId, record.priority()));
        }
    }

    private void dictionaryUseDataTableTestCreateTable(Connection connection) throws Exception {
        // テストで必要なテーブルを作成
        dao.serverKeyTable().createTableIfNotExists(connection);
        dao.dictionaryKeyTable().createTableIfNotExists(connection);

        dao.dictionaryUseDataTable().createTableIfNotExists(connection);

        // 作成済みの場合にエラーが出ないか確認
        dao.dictionaryUseDataTable().createTableIfNotExists(connection);
    }

    private Stream<DictionaryUseDataRecord> dictionaryUseDataTableTestRecordData() {
        return dictionaryUseDataRecordData()
                .map(data -> new DictionaryUseDataRecord(data.enable(), data.priority()));
    }

    private void dictionaryUseDataTableTestSelectCheck(Connection connection, ServerDictionaryKey key, DictionaryUseDataRecord expectedRecord) throws Exception {
        /* 取得した値が期待するレコードと一致するか確認 */

        // キーから取得して確認
        Optional<IdRecordPair<DictionaryUseDataRecord>> retIdAndRecord = dao.dictionaryUseDataTable().selectRecordByKey(connection, key);
        assertTrue(retIdAndRecord.isPresent());
        assertEquals(expectedRecord, retIdAndRecord.get().getRecord());

        int tableId = retIdAndRecord.get().getId();

        // テーブルIDから取得して確認
        Optional<DictionaryUseDataRecord> retRecordById = dao.dictionaryUseDataTable().selectRecordById(connection, tableId);
        assertTrue(retRecordById.isPresent());
        assertEquals(expectedRecord, retRecordById.get());

        // レコードの値を単体で取得して確認
        assertEquals(expectedRecord.enable(), dao.dictionaryUseDataTable().selectEnable(connection, tableId).orElse(null));
        assertEquals(TestUtils.getOptionalIntByInteger(expectedRecord.priority()), dao.dictionaryUseDataTable().selectPriority(connection, tableId));
    }

    @Test
    void testDictionaryUseDataSelectAllEntry() throws Exception {
        try (Connection connection = dao.getConnection()) {
            dictionaryUseDataTableTestCreateTable(connection);

            assertTrue(dao.dictionaryUseDataTable().selectAll(connection, insertAndSelectKeyId(connection, dao.serverKeyTable(), 114L)).isEmpty());
            assertTrue(dao.dictionaryUseDataTable().selectAll(connection, insertAndSelectKeyId(connection, dao.serverKeyTable(), 514L)).isEmpty());
            assertTrue(dao.dictionaryUseDataTable().selectAll(connection, insertAndSelectKeyId(connection, dao.serverKeyTable(), 810L)).isEmpty());
            assertTrue(dao.dictionaryUseDataTable().selectAll(connection, insertAndSelectKeyId(connection, dao.serverKeyTable(), 364L)).isEmpty());
            assertTrue(dao.dictionaryUseDataTable().selectAll(connection, insertAndSelectKeyId(connection, dao.serverKeyTable(), 110L)).isEmpty());

            TestUtils.testForEach(dictionaryIdsData(), dictId -> dao.dictionaryUseDataTable().insertRecordIfNotExists(connection,
                    new ServerDictionaryKey(insertAndSelectKeyId(connection, dao.serverKeyTable(), 114L), insertAndSelectKeyId(connection, dao.dictionaryKeyTable(), dictId)),
                    new DictionaryUseDataRecord(false, 0)));

            TestUtils.forEachIndexed(dictionaryIdsData(), (idx, dictId) -> dao.dictionaryUseDataTable().insertRecordIfNotExists(connection,
                    new ServerDictionaryKey(insertAndSelectKeyId(connection, dao.serverKeyTable(), 514L), insertAndSelectKeyId(connection, dao.dictionaryKeyTable(), dictId)),
                    new DictionaryUseDataRecord(true, idx)));

            dao.dictionaryUseDataTable().insertRecordIfNotExists(connection,
                    new ServerDictionaryKey(insertAndSelectKeyId(connection, dao.serverKeyTable(), 810L), insertAndSelectKeyId(connection, dao.dictionaryKeyTable(), "abbreviation")),
                    new DictionaryUseDataRecord(null, -5));
            dao.dictionaryUseDataTable().insertRecordIfNotExists(connection,
                    new ServerDictionaryKey(insertAndSelectKeyId(connection, dao.serverKeyTable(), 810L), insertAndSelectKeyId(connection, dao.dictionaryKeyTable(), "global")),
                    new DictionaryUseDataRecord(false, 3));
            dao.dictionaryUseDataTable().insertRecordIfNotExists(connection,
                    new ServerDictionaryKey(insertAndSelectKeyId(connection, dao.serverKeyTable(), 810L), insertAndSelectKeyId(connection, dao.dictionaryKeyTable(), "romaji")),
                    new DictionaryUseDataRecord(true, 0));
            dao.dictionaryUseDataTable().insertRecordIfNotExists(connection,
                    new ServerDictionaryKey(insertAndSelectKeyId(connection, dao.serverKeyTable(), 810L), insertAndSelectKeyId(connection, dao.dictionaryKeyTable(), "server")),
                    new DictionaryUseDataRecord(null, 1));
            dao.dictionaryUseDataTable().insertRecordIfNotExists(connection,
                    new ServerDictionaryKey(insertAndSelectKeyId(connection, dao.serverKeyTable(), 810L), insertAndSelectKeyId(connection, dao.dictionaryKeyTable(), "unit")),
                    new DictionaryUseDataRecord(true, null));

            TestUtils.forEachIndexed(dictionaryIdsData(), (idx, dictId) -> dao.dictionaryUseDataTable().insertRecordIfNotExists(connection,
                    new ServerDictionaryKey(insertAndSelectKeyId(connection, dao.serverKeyTable(), 110L), insertAndSelectKeyId(connection, dao.dictionaryKeyTable(), dictId)),
                    new DictionaryUseDataRecord(null, null)));

            List<DictionaryUseEntry> ret1 = dao.dictionaryUseDataTable().selectAll(connection, insertAndSelectKeyId(connection, dao.serverKeyTable(), 114L));
            List<DictionaryUseEntry> ret2 = dao.dictionaryUseDataTable().selectAll(connection, insertAndSelectKeyId(connection, dao.serverKeyTable(), 514L));
            List<DictionaryUseEntry> ret3 = dao.dictionaryUseDataTable().selectAll(connection, insertAndSelectKeyId(connection, dao.serverKeyTable(), 810L));
            List<DictionaryUseEntry> ret4 = dao.dictionaryUseDataTable().selectAll(connection, insertAndSelectKeyId(connection, dao.serverKeyTable(), 364L));
            List<DictionaryUseEntry> ret5 = dao.dictionaryUseDataTable().selectAll(connection, insertAndSelectKeyId(connection, dao.serverKeyTable(), 110L));

            List<DictionaryUseEntry> expectedRet1 = new ArrayList<>();
            TestUtils.forEachIndexed(dictionaryIdsData(), (idx, dictId) -> expectedRet1.add(new DictionaryUseEntry(dictId, false, 0)));
            assertTrue(!ret1.isEmpty() && CollectionUtils.isEqualCollection(expectedRet1, ret1));

            List<DictionaryUseEntry> expectedRet2 = new ArrayList<>();
            TestUtils.forEachIndexed(dictionaryIdsData(), (idx, dictId) -> expectedRet2.add(new DictionaryUseEntry(dictId, true, idx)));
            assertTrue(!ret2.isEmpty() && CollectionUtils.isEqualCollection(expectedRet2, ret2));

            assertEquals(5, ret3.size());
            assertTrue(ret3.contains(new DictionaryUseEntry("abbreviation", null, -5)));
            assertTrue(ret3.contains(new DictionaryUseEntry("global", false, 3)));
            assertTrue(ret3.contains(new DictionaryUseEntry("romaji", true, 0)));
            assertTrue(ret3.contains(new DictionaryUseEntry("server", null, 1)));
            assertTrue(ret3.contains(new DictionaryUseEntry("unit", true, null)));

            assertTrue(ret4.isEmpty());

            List<DictionaryUseEntry> expectedRet5 = new ArrayList<>();
            TestUtils.forEachIndexed(dictionaryIdsData(), (idx, dictId) -> expectedRet5.add(new DictionaryUseEntry(dictId, null, null)));
            assertTrue(!ret5.isEmpty() && CollectionUtils.isEqualCollection(expectedRet5, ret5));
        }
    }

    // BotStateData

    @Test
    void testBotStateDataTableInsert() throws Exception {
        try (Connection connection = dao.getConnection()) {
            botStateDataTableTestCreateTable(connection);

            TestUtils.testForEach(createTestDataStream(discordServerIdsData(), discordBotIdsData()), serverIdAndBotId -> {
                int serverKeyId = insertAndSelectKeyId(connection, dao.serverKeyTable(), serverIdAndBotId.getLeft());
                int botKeyId = insertAndSelectKeyId(connection, dao.botKeyTable(), serverIdAndBotId.getRight());

                ServerBotKey serverBotKey = new ServerBotKey(serverKeyId, botKeyId);
                BotStateDataRecord[] firstRecord = new BotStateDataRecord[1];

                TestUtils.forEachIndexed(botStateDataTableTestRecordData(connection), (idx, record) -> {
                    dao.botStateDataTable().insertRecordIfNotExists(connection, serverBotKey, record);
                    if (idx == 0) {
                        // 追加したレコードと取得できるレコードが一致する確認
                        botStateDataTableTestSelectCheck(connection, serverBotKey, record);
                        firstRecord[0] = record;
                    } else {
                        // 既にレコードが存在している状態でInsertした場合に、上書きされないことを確認
                        botStateDataTableTestSelectCheck(connection, serverBotKey, firstRecord[0]);
                    }
                });
            });
        }
    }

    @Test
    void testBotStateDataTableUpdate() throws Exception {
        try (Connection connection = dao.getConnection()) {
            botStateDataTableTestCreateTable(connection);

            BotStateDataRecord initRecord = new BotStateDataRecord(null, null, null, null);

            TestUtils.testForEach(createTestDataStream(discordServerIdsData(), discordBotIdsData()), serverIdAndBotId -> {
                int serverKeyId = insertAndSelectKeyId(connection, dao.serverKeyTable(), serverIdAndBotId.getLeft());
                int botKeyId = insertAndSelectKeyId(connection, dao.botKeyTable(), serverIdAndBotId.getRight());
                ServerBotKey serverBotKey = new ServerBotKey(serverKeyId, botKeyId);

                dao.botStateDataTable().insertRecordIfNotExists(connection, serverBotKey, initRecord);

                Optional<IdRecordPair<BotStateDataRecord>> preIdAndRecord = dao.botStateDataTable().selectRecordByKey(connection, serverBotKey);
                int recordId = preIdAndRecord.orElseThrow().getId();

                TestUtils.testForEach(botStateDataTableTestRecordData(connection), record -> {
                    // 単体でレコードの値を更新

                    TTSChannelKeyPair connectedChannel = null;
                    TTSChannelKeyPair reconnectConnectedChannel = null;

                    if (record.speakAudioChannelKey() != null && record.readTextChannelKey() != null) {
                        connectedChannel = new TTSChannelKeyPair(record.speakAudioChannelKey(), record.readTextChannelKey());
                    }

                    if (record.reconnectSpeakAudioChannelKey() != null && record.reconnectReadTextChannelKey() != null) {
                        reconnectConnectedChannel = new TTSChannelKeyPair(record.reconnectSpeakAudioChannelKey(), record.reconnectReadTextChannelKey());
                    }

                    dao.botStateDataTable().updateConnectedChannelKeyPair(connection, recordId, connectedChannel);
                    dao.botStateDataTable().updateReconnectChannelKeyPair(connection, recordId, reconnectConnectedChannel);

                    botStateDataTableTestSelectCheck(connection, serverBotKey, record);

                    // 単体で更新
                    if (connectedChannel != null) {
                        dao.botStateDataTable().updateConnectedChannelKeyPair(connection, recordId,
                                new TTSChannelKeyPair(
                                        insertAndSelectKeyId(connection, dao.channelKeyTable(), dao.channelKeyTable().selectKey(connection, connectedChannel.speakAudioChannelKey()).orElseThrow() + 1),
                                        insertAndSelectKeyId(connection, dao.channelKeyTable(), dao.channelKeyTable().selectKey(connection, connectedChannel.readTextChannelKey()).orElseThrow() + 1)
                                ));

                        dao.botStateDataTable().updateSpeakAudioChannel(connection, recordId, connectedChannel.speakAudioChannelKey());
                        dao.botStateDataTable().updateReadAroundTextChannel(connection, recordId, connectedChannel.readTextChannelKey());
                    } else {
                        dao.botStateDataTable().updateConnectedChannelKeyPair(connection, recordId,
                                new TTSChannelKeyPair(
                                        insertAndSelectKeyId(connection, dao.channelKeyTable(), 10L),
                                        insertAndSelectKeyId(connection, dao.channelKeyTable(), 20L)
                                ));

                        dao.botStateDataTable().updateSpeakAudioChannel(connection, recordId, null);
                        dao.botStateDataTable().updateReadAroundTextChannel(connection, recordId, null);
                    }

                    botStateDataTableTestSelectCheck(connection, serverBotKey, record);
                });
            });
        }
    }

    @Test
    void testBotStateDataTableNotExist() throws Exception {
        try (Connection connection = dao.getConnection()) {
            botStateDataTableTestCreateTable(connection);

            int serverKeyId = insertAndSelectKeyId(connection, dao.serverKeyTable(), 114514L);
            int botKeyId = insertAndSelectKeyId(connection, dao.botKeyTable(), 810L);
            ServerBotKey serverBotKey = new ServerBotKey(serverKeyId, botKeyId);

            // 存在しないキーからレコードを取得
            assertTrue(dao.botStateDataTable().selectRecordByKey(connection, serverBotKey).isEmpty());

            int recordId = 3;

            // 存在しないレコードIDで取得
            assertTrue(dao.botStateDataTable().selectRecordById(connection, recordId).isEmpty());

            // 存在しないレコードIDで取得(単体)
            assertThrows(IllegalStateException.class, () -> dao.botStateDataTable().selectConnectedChannelKeyPair(connection, recordId));
            assertThrows(IllegalStateException.class, () -> dao.botStateDataTable().selectReconnectChannelKeyPair(connection, recordId));

            // 存在しないレコードIDで更新
            assertThrows(IllegalStateException.class, () -> dao.botStateDataTable().updateConnectedChannelKeyPair(connection, recordId, null));
            assertThrows(IllegalStateException.class, () -> dao.botStateDataTable().updateReconnectChannelKeyPair(connection, recordId, null));
        }
    }

    private void botStateDataTableTestCreateTable(Connection connection) throws Exception {
        // テストで必要なテーブルを作成
        dao.serverKeyTable().createTableIfNotExists(connection);
        dao.botKeyTable().createTableIfNotExists(connection);
        dao.channelKeyTable().createTableIfNotExists(connection);

        dao.botStateDataTable().createTableIfNotExists(connection);

        // 作成済みの場合にエラーが出ないか確認
        dao.botStateDataTable().createTableIfNotExists(connection);
    }

    private Stream<BotStateDataRecord> botStateDataTableTestRecordData(Connection connection) {
        return botStatePairsData()
                .map(it -> {
                    try {
                        Integer speakAudioChannelKey = insertAndSelectKeyId(connection, dao.channelKeyTable(), it.getLeft() == null ? null : it.getLeft().speakAudioChannel());
                        Integer readTextChannelKey = insertAndSelectKeyId(connection, dao.channelKeyTable(), it.getLeft() == null ? null : it.getLeft().readTextChannel());
                        Integer reconnectSpeakAudioChannelKey = insertAndSelectKeyId(connection, dao.channelKeyTable(), it.getRight() == null ? null : it.getRight().speakAudioChannel());
                        Integer reconnectReadTextChannelKey = insertAndSelectKeyId(connection, dao.channelKeyTable(), it.getRight() == null ? null : it.getRight().readTextChannel());
                        return new BotStateDataRecord(speakAudioChannelKey, readTextChannelKey, reconnectSpeakAudioChannelKey, reconnectReadTextChannelKey);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private void botStateDataTableTestSelectCheck(Connection connection, ServerBotKey key, BotStateDataRecord expectedRecord) throws Exception {
        /* 取得した値が期待するレコードと一致するか確認 */

        // キーから取得して確認
        Optional<IdRecordPair<BotStateDataRecord>> retIdAndRecord = dao.botStateDataTable().selectRecordByKey(connection, key);
        assertTrue(retIdAndRecord.isPresent());
        assertEquals(expectedRecord, retIdAndRecord.get().getRecord());

        int recordId = retIdAndRecord.get().getId();

        // テーブルIDから取得して確認
        Optional<BotStateDataRecord> retRecordById = dao.botStateDataTable().selectRecordById(connection, recordId);
        assertTrue(retRecordById.isPresent());
        assertEquals(expectedRecord, retRecordById.get());

        // レコードの値を単体で取得して確認

        TTSChannelKeyPair connectedChannel = null;
        TTSChannelKeyPair reconnectConnectedChannel = null;

        if (expectedRecord.speakAudioChannelKey() != null && expectedRecord.readTextChannelKey() != null) {
            connectedChannel = new TTSChannelKeyPair(expectedRecord.speakAudioChannelKey(), expectedRecord.readTextChannelKey());
        }

        if (expectedRecord.reconnectSpeakAudioChannelKey() != null && expectedRecord.reconnectReadTextChannelKey() != null) {
            reconnectConnectedChannel = new TTSChannelKeyPair(expectedRecord.reconnectSpeakAudioChannelKey(), expectedRecord.reconnectReadTextChannelKey());
        }

        assertEquals(connectedChannel, dao.botStateDataTable().selectConnectedChannelKeyPair(connection, recordId).orElse(null));
        assertEquals(reconnectConnectedChannel, dao.botStateDataTable().selectReconnectChannelKeyPair(connection, recordId).orElse(null));

        // 単体で取得
        if (connectedChannel != null) {
            assertEquals(connectedChannel.speakAudioChannelKey(), dao.botStateDataTable().selectSpeakAudioChannel(connection, recordId).orElseThrow());
            assertEquals(connectedChannel.readTextChannelKey(), dao.botStateDataTable().selectReadAroundTextChannel(connection, recordId).orElseThrow());
        }
    }

    @Test
    void testBotStateDataTableSelectAllConnectedChannelPairByBotKeyId() throws Exception {
        try (Connection connection = dao.getConnection()) {
            botStateDataTableTestCreateTable(connection);

            ServerBotKey serverBotKey1 =
                    new ServerBotKey(insertAndSelectKeyId(connection, dao.serverKeyTable(), 114514L), insertAndSelectKeyId(connection, dao.botKeyTable(), 110L));
            ServerBotKey serverBotKey2 =
                    new ServerBotKey(insertAndSelectKeyId(connection, dao.serverKeyTable(), 114514L), insertAndSelectKeyId(connection, dao.botKeyTable(), 13L));
            ServerBotKey serverBotKey3 =
                    new ServerBotKey(insertAndSelectKeyId(connection, dao.serverKeyTable(), 364364L), insertAndSelectKeyId(connection, dao.botKeyTable(), 110L));
            ServerBotKey serverBotKey4 =
                    new ServerBotKey(insertAndSelectKeyId(connection, dao.serverKeyTable(), 810L), insertAndSelectKeyId(connection, dao.botKeyTable(), 200L));
            ServerBotKey serverBotKey5 =
                    new ServerBotKey(insertAndSelectKeyId(connection, dao.serverKeyTable(), 114514L), insertAndSelectKeyId(connection, dao.botKeyTable(), 200L));

            dao.botStateDataTable().insertRecordIfNotExists(connection, serverBotKey1,
                    new BotStateDataRecord(
                            insertAndSelectKeyId(connection, dao.channelKeyTable(), 10L),
                            insertAndSelectKeyId(connection, dao.channelKeyTable(), 20L),
                            null, null));
            dao.botStateDataTable().insertRecordIfNotExists(connection, serverBotKey2,
                    new BotStateDataRecord(
                            insertAndSelectKeyId(connection, dao.channelKeyTable(), 30L),
                            insertAndSelectKeyId(connection, dao.channelKeyTable(), 40L),
                            null, null));
            dao.botStateDataTable().insertRecordIfNotExists(connection, serverBotKey3,
                    new BotStateDataRecord(
                            insertAndSelectKeyId(connection, dao.channelKeyTable(), 50L),
                            insertAndSelectKeyId(connection, dao.channelKeyTable(), 60L),
                            null, null));
            dao.botStateDataTable().insertRecordIfNotExists(connection, serverBotKey4,
                    new BotStateDataRecord(
                            insertAndSelectKeyId(connection, dao.channelKeyTable(), 70L),
                            insertAndSelectKeyId(connection, dao.channelKeyTable(), 80L),
                            null, null));
            dao.botStateDataTable().insertRecordIfNotExists(connection, serverBotKey5,
                    new BotStateDataRecord(
                            null,
                            null,
                            null, null));

            Map<Long, TTSChannelPair> allConnectedChannel1 = dao.botStateDataTable().selectAllConnectedChannelPairByBotKeyId(connection, insertAndSelectKeyId(connection, dao.botKeyTable(), 110L));
            assertEquals(2, allConnectedChannel1.size());
            assertEquals(new TTSChannelPair(10L, 20L), allConnectedChannel1.get(114514L));
            assertEquals(new TTSChannelPair(50L, 60L), allConnectedChannel1.get(364364L));

            Map<Long, TTSChannelPair> allConnectedChannel2 = dao.botStateDataTable().selectAllConnectedChannelPairByBotKeyId(connection, insertAndSelectKeyId(connection, dao.botKeyTable(), 10L));
            assertTrue(allConnectedChannel2.isEmpty());
        }
    }

    // ServerCustomDictionaryTable

    @Test
    void testServerCustomDictionaryTable() throws Exception {
        try (Connection connection = dao.getConnection()) {
            DAO.ServerCustomDictionaryTable serverCustomDictionaryTable = dao.serverCustomDictionaryTable();

            // テーブルの作成
            serverCustomDictionaryTable.createTableIfNotExists(connection);
            dao.serverKeyTable().createTableIfNotExists(connection);
            dao.dictionaryReplaceTypeKeyTable().createTableIfNotExists(connection);

            // 作成済みの場合にエラーが出ないか確認
            serverCustomDictionaryTable.createTableIfNotExists(connection);

            ServerKey serverKey1 = new ServerKey(insertAndSelectKeyId(connection, dao.serverKeyTable(), 810L));
            ServerKey serverKey2 = new ServerKey(insertAndSelectKeyId(connection, dao.serverKeyTable(), 114514L));
            ServerKey serverKey3 = new ServerKey(insertAndSelectKeyId(connection, dao.serverKeyTable(), 63464L));

            // 辞書が空かどうか確認
            assertTrue(serverCustomDictionaryTable.selectRecords(connection, serverKey1).isEmpty());
            assertTrue(serverCustomDictionaryTable.selectRecords(connection, serverKey2).isEmpty());
            assertTrue(serverCustomDictionaryTable.selectRecords(connection, serverKey3).isEmpty());

            // 辞書を追加 1,2は別々の辞書エントリを追加して3は追加しない

            List<DictionaryRecord> dictEntries1 = new ArrayList<>();
            List<DictionaryRecord> dictEntries2 = new ArrayList<>();

            TestUtils.testForEach(createTestDataStream(customDictionaryData1(), Arrays.stream(ReplaceType.values())), data -> {
                int replaceTypeId = insertAndSelectKeyId(connection, dao.dictionaryReplaceTypeKeyTable(), data.getRight().getName());
                DictionaryRecord record = new DictionaryRecord(data.getKey().getLeft(), data.getKey().getRight(), replaceTypeId);
                dictEntries1.add(record);
            });
            TestUtils.testForEach(createTestDataStream(customDictionaryData2(), Arrays.stream(ReplaceType.values())), data -> {
                int replaceTypeId = insertAndSelectKeyId(connection, dao.dictionaryReplaceTypeKeyTable(), data.getRight().getName());
                DictionaryRecord record = new DictionaryRecord(data.getKey().getLeft(), data.getKey().getRight(), replaceTypeId);
                dictEntries2.add(record);
            });

            TestUtils.testForEach(dictEntries1.stream(), record -> serverCustomDictionaryTable.insertRecord(connection, serverKey1, record));
            TestUtils.testForEach(dictEntries2.stream(), record -> serverCustomDictionaryTable.insertRecord(connection, serverKey2, record));

            // 辞書を取得
            Map<Integer, DictionaryRecord> ret1 = serverCustomDictionaryTable.selectRecords(connection, serverKey1);
            Map<Integer, DictionaryRecord> ret2 = serverCustomDictionaryTable.selectRecords(connection, serverKey2);
            Map<Integer, DictionaryRecord> ret3 = serverCustomDictionaryTable.selectRecords(connection, serverKey3);

            // 追加したエントリと取得したエントリが同じか確認
            assertTrue(!dictEntries1.isEmpty() && CollectionUtils.isEqualCollection(dictEntries1, ret1.values()));
            assertTrue(!dictEntries2.isEmpty() && CollectionUtils.isEqualCollection(dictEntries2, ret2.values()));

            // 追加していない場合は、取得しても空かどうか確認
            assertTrue(ret3.isEmpty());

            // 辞書を削除
            TestUtils.testForEach(ret1.keySet().stream(), id -> serverCustomDictionaryTable.deleteRecord(connection, id));

            // 削除できているか確認
            Map<Integer, DictionaryRecord> delRet1 = serverCustomDictionaryTable.selectRecords(connection, serverKey1);
            Map<Integer, DictionaryRecord> delRet2 = serverCustomDictionaryTable.selectRecords(connection, serverKey2);
            Map<Integer, DictionaryRecord> delRet3 = serverCustomDictionaryTable.selectRecords(connection, serverKey3);

            assertTrue(delRet1.isEmpty());
            assertTrue(!dictEntries2.isEmpty() && CollectionUtils.isEqualCollection(dictEntries2, delRet2.values()));
            assertTrue(delRet3.isEmpty());
        }
    }

    @Test
    void testServerCustomDictionaryTableSelectByTarget() throws Exception {
        try (Connection connection = dao.getConnection()) {
            DAO.ServerCustomDictionaryTable serverCustomDictionaryTable = dao.serverCustomDictionaryTable();

            // テーブルの作成
            serverCustomDictionaryTable.createTableIfNotExists(connection);
            dao.serverKeyTable().createTableIfNotExists(connection);
            dao.dictionaryReplaceTypeKeyTable().createTableIfNotExists(connection);

            ServerKey serverKey1 = new ServerKey(insertAndSelectKeyId(connection, dao.serverKeyTable(), 810L));
            ServerKey serverKey2 = new ServerKey(insertAndSelectKeyId(connection, dao.serverKeyTable(), 114514L));
            ServerKey serverKey3 = new ServerKey(insertAndSelectKeyId(connection, dao.serverKeyTable(), 63464L));

            // 辞書を追加
            int replaceTypeId = insertAndSelectKeyId(connection, dao.dictionaryReplaceTypeKeyTable(), ReplaceType.CHARACTER.getName());
            serverCustomDictionaryTable.insertRecord(connection, serverKey1, new DictionaryRecord("課長", "壊れる", replaceTypeId));
            serverCustomDictionaryTable.insertRecord(connection, serverKey1, new DictionaryRecord("kbtit", "kbhit", replaceTypeId));
            serverCustomDictionaryTable.insertRecord(connection, serverKey1, new DictionaryRecord("課長", "壊れちゃ＾～う", replaceTypeId));
            serverCustomDictionaryTable.insertRecord(connection, serverKey1, new DictionaryRecord("yj", "senpai", replaceTypeId));

            serverCustomDictionaryTable.insertRecord(connection, serverKey2, new DictionaryRecord("114", "いいよ", replaceTypeId));
            serverCustomDictionaryTable.insertRecord(connection, serverKey2, new DictionaryRecord("ikisugi", "イキスギ", replaceTypeId));
            serverCustomDictionaryTable.insertRecord(connection, serverKey2, new DictionaryRecord("nu", "nu!", replaceTypeId));
            serverCustomDictionaryTable.insertRecord(connection, serverKey2, new DictionaryRecord("fa", "fa!?", replaceTypeId));

            // 読みから取得できるか確認
            Map<Integer, DictionaryRecord> ret1_1 = serverCustomDictionaryTable.selectRecordByTarget(connection, serverKey1, "課長");
            Map<Integer, DictionaryRecord> ret1_2 = serverCustomDictionaryTable.selectRecordByTarget(connection, serverKey1, "許して");

            assertEquals(2, ret1_1.size());
            ret1_1.forEach((id, record) -> assertEquals("課長", record.target()));
            assertTrue(ret1_2.isEmpty());

            Map<Integer, DictionaryRecord> ret2_1 = serverCustomDictionaryTable.selectRecordByTarget(connection, serverKey2, "ikisugi");
            Map<Integer, DictionaryRecord> ret2_2 = serverCustomDictionaryTable.selectRecordByTarget(connection, serverKey2, "課長");
            assertEquals(1, ret2_1.size());
            assertEquals("ikisugi", ret2_1.entrySet().stream().findFirst().orElseThrow().getValue().target());
            assertEquals("イキスギ", ret2_1.entrySet().stream().findFirst().orElseThrow().getValue().read());
            assertTrue(ret2_2.isEmpty());

            Map<Integer, DictionaryRecord> ret3 = serverCustomDictionaryTable.selectRecordByTarget(connection, serverKey3, "");
            assertTrue(ret3.isEmpty());
        }
    }

    // GlobalCustomDictionaryTable

    @Test
    void testGlobalCustomDictionaryTable() throws Exception {
        try (Connection connection = dao.getConnection()) {
            DAO.GlobalCustomDictionaryTable globalCustomDictionaryTable = dao.globalCustomDictionaryTable();

            // テーブルの作成
            globalCustomDictionaryTable.createTableIfNotExists(connection);
            dao.dictionaryReplaceTypeKeyTable().createTableIfNotExists(connection);

            // 作成済みの場合にエラーが出ないか確認
            globalCustomDictionaryTable.createTableIfNotExists(connection);


            // 辞書が空かどうか確認
            assertTrue(globalCustomDictionaryTable.selectRecords(connection).isEmpty());

            // 辞書を追加
            List<DictionaryRecord> dictEntries = new ArrayList<>();
            TestUtils.testForEach(createTestDataStream(customDictionaryData1(), Arrays.stream(ReplaceType.values())), data -> {
                int replaceTypeId = insertAndSelectKeyId(connection, dao.dictionaryReplaceTypeKeyTable(), data.getRight().getName());
                DictionaryRecord record = new DictionaryRecord(data.getKey().getLeft(), data.getKey().getRight(), replaceTypeId);
                dictEntries.add(record);
            });

            TestUtils.testForEach(dictEntries.stream(), record -> globalCustomDictionaryTable.insertRecord(connection, record));

            // 辞書を取得
            Map<Integer, DictionaryRecord> ret = globalCustomDictionaryTable.selectRecords(connection);

            // 追加したエントリと取得したエントリが同じか確認
            assertTrue(!dictEntries.isEmpty() && CollectionUtils.isEqualCollection(dictEntries, ret.values()));

            // 辞書を削除
            TestUtils.testForEach(ret.keySet().stream(), id -> globalCustomDictionaryTable.deleteRecord(connection, id));

            // 削除できているか確認
            Map<Integer, DictionaryRecord> delRet = globalCustomDictionaryTable.selectRecords(connection);
            assertTrue(delRet.isEmpty());
        }
    }

    @Test
    void testGlobalCustomDictionaryTableSelectByTarget() throws Exception {
        try (Connection connection = dao.getConnection()) {
            DAO.GlobalCustomDictionaryTable globalCustomDictionaryTable = dao.globalCustomDictionaryTable();

            // テーブルの作成
            globalCustomDictionaryTable.createTableIfNotExists(connection);
            dao.dictionaryReplaceTypeKeyTable().createTableIfNotExists(connection);

            // 辞書を追加
            int replaceTypeId = insertAndSelectKeyId(connection, dao.dictionaryReplaceTypeKeyTable(), ReplaceType.CHARACTER.getName());
            globalCustomDictionaryTable.insertRecord(connection, new DictionaryRecord("課長", "壊れる", replaceTypeId));
            globalCustomDictionaryTable.insertRecord(connection, new DictionaryRecord("kbtit", "kbhit", replaceTypeId));
            globalCustomDictionaryTable.insertRecord(connection, new DictionaryRecord("課長", "壊れちゃ＾～う", replaceTypeId));
            globalCustomDictionaryTable.insertRecord(connection, new DictionaryRecord("yj", "senpai", replaceTypeId));
            globalCustomDictionaryTable.insertRecord(connection, new DictionaryRecord("114", "いいよ", replaceTypeId));
            globalCustomDictionaryTable.insertRecord(connection, new DictionaryRecord("ikisugi", "イキスギ", replaceTypeId));
            globalCustomDictionaryTable.insertRecord(connection, new DictionaryRecord("nu", "nu!", replaceTypeId));
            globalCustomDictionaryTable.insertRecord(connection, new DictionaryRecord("fa", "fa!?", replaceTypeId));

            // 読みから取得できるか確認
            Map<Integer, DictionaryRecord> ret1 = globalCustomDictionaryTable.selectRecordByTarget(connection, "課長");
            Map<Integer, DictionaryRecord> ret2 = globalCustomDictionaryTable.selectRecordByTarget(connection, "許して");
            Map<Integer, DictionaryRecord> ret3 = globalCustomDictionaryTable.selectRecordByTarget(connection, "ikisugi");
            Map<Integer, DictionaryRecord> ret5 = globalCustomDictionaryTable.selectRecordByTarget(connection, "");

            assertEquals(2, ret1.size());
            ret1.forEach((id, record) -> assertEquals("課長", record.target()));

            assertTrue(ret2.isEmpty());

            assertEquals(1, ret3.size());
            assertEquals("ikisugi", ret3.entrySet().stream().findFirst().orElseThrow().getValue().target());
            assertEquals("イキスギ", ret3.entrySet().stream().findFirst().orElseThrow().getValue().read());

            assertTrue(ret5.isEmpty());
        }
    }

    // Common

    private <T> Integer insertAndSelectKeyId(Connection connection, DAO.KeyTable<T> table, T key) throws SQLException {
        // キーを追加してIDを取得

        if (key == null) {
            return null;
        }

        table.insertKeyIfNotExists(connection, key);
        return table.selectId(connection, key).orElseThrow();
    }

}
