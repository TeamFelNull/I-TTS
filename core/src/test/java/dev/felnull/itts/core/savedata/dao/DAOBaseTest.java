package dev.felnull.itts.core.savedata.dao;

import com.google.common.collect.ImmutableList;
import dev.felnull.itts.core.dict.ReplaceType;
import dev.felnull.itts.core.discord.AutoDisconnectMode;
import dev.felnull.itts.core.savedata.AbstractSaveDataTest;
import dev.felnull.itts.core.util.TestUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public abstract class DAOBaseTest extends AbstractSaveDataTest {

    private static final ServerDataRecord[] SERVER_DATA_RECORDS = new ServerDataRecord[]{
            new ServerDataRecord(null, null, true, true, false, 0, 15, 0),
            new ServerDataRecord(null, "", false, true, true, 100, 0, 0),
            new ServerDataRecord(null, null, false, false, false, 364, 12, 0),
            new ServerDataRecord(null, "(;).*", true, false, false, 130, 1, 0),
            new ServerDataRecord(null, "(!|/|\\\\$|`).*", false, true, false, Integer.MAX_VALUE, 5, 0),
            new ServerDataRecord(null, "ikisugi", false, false, true, 30, Integer.MAX_VALUE, 0),
            new ServerDataRecord(null, "F.C.O.H", true, true, true, 1, 40, 0)
    };

    private static final ServerUserDataRecord[] SERVER_USER_DATA_RECORDS = new ServerUserDataRecord[]{
            new ServerUserDataRecord(null, false, null),
            new ServerUserDataRecord(null, true, null),
            new ServerUserDataRecord(null, false, "野獣先輩"),
            new ServerUserDataRecord(null, true, "NKTIDKSG")
    };

    private static final DictionaryUseDataRecord[] DICTIONARY_USE_DATA_RECORDS = new DictionaryUseDataRecord[]{
            new DictionaryUseDataRecord(null, 0),
            new DictionaryUseDataRecord(true, 1),
            new DictionaryUseDataRecord(false, -1),
    };

    private static final List<Pair<Pair<Long, Long>, Pair<Long, Long>>> BOT_STATE_DATA_PAIR = ImmutableList.of(
            Pair.of(null, null),
            Pair.of(Pair.of(1919L, 810L), null),
            Pair.of(null, Pair.of(364364L, 114514L)),
            Pair.of(Pair.of(1919L, 810L), Pair.of(364364L, 114514L))
    );

    protected static DAO dao;

    @AfterAll
    static void afterAll() throws IOException {
        dao.close();
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
            TestUtils.testForEach(dictionaryNamesData(), it -> keyTableTest(connection, ids, dao.dictionaryKeyTable(), it));
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
        dao.serverDataTable().createTableIfNotExists(connection);
        dao.serverKeyTable().createTableIfNotExists(connection);
        dao.voiceTypeKeyTable().createTableIfNotExists(connection);
        dao.autoDisconnectModeKeyTable().createTableIfNotExists(connection);
    }

    private Stream<ServerDataRecord> serverDataTableTestRecordData(Connection connection) {
        // テストで使うデータのストリームを作成
        return createTestDataStream(Arrays.stream(SERVER_DATA_RECORDS), voiceTypeNamesNullableData(), autoDisconnectModesData().map(AutoDisconnectMode::getName))
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

            TestUtils.testForEach(createTestDataStream(discordUserIdsData().boxed(), Arrays.stream(SERVER_USER_DATA_RECORDS)), data -> {
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
        dao.serverUserDataTable().createTableIfNotExists(connection);
        dao.serverKeyTable().createTableIfNotExists(connection);
        dao.userKeyTable().createTableIfNotExists(connection);
        dao.voiceTypeKeyTable().createTableIfNotExists(connection);
    }

    private Stream<ServerUserDataRecord> serverUserDataTableTestRecordData(Connection connection) {
        return createTestDataStream(Arrays.stream(SERVER_USER_DATA_RECORDS), voiceTypeNamesNullableData())
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

            TestUtils.testForEach(createTestDataStream(discordServerIdsData().boxed(), dictionaryNamesData()), serverIdAndDictName -> {
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

            TestUtils.testForEach(createTestDataStream(discordServerIdsData().boxed(), dictionaryNamesData()), serverIdAndDictName -> {
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
        dao.dictionaryUseDataTable().createTableIfNotExists(connection);
        dao.serverKeyTable().createTableIfNotExists(connection);
        dao.dictionaryKeyTable().createTableIfNotExists(connection);
    }

    private Stream<DictionaryUseDataRecord> dictionaryUseDataTableTestRecordData() {
        return Arrays.stream(DICTIONARY_USE_DATA_RECORDS)
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
        assertEquals(expectedRecord.priority(), dao.dictionaryUseDataTable().selectPriority(connection, tableId));
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
                int tableId = preIdAndRecord.orElseThrow().getId();

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

                    dao.botStateDataTable().updateConnectedChannelKeyPair(connection, tableId, connectedChannel);
                    dao.botStateDataTable().updateReconnectChannelKeyPair(connection, tableId, reconnectConnectedChannel);

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
        dao.botStateDataTable().createTableIfNotExists(connection);
        dao.serverKeyTable().createTableIfNotExists(connection);
        dao.botKeyTable().createTableIfNotExists(connection);
        dao.channelKeyTable().createTableIfNotExists(connection);
    }

    private Stream<BotStateDataRecord> botStateDataTableTestRecordData(Connection connection) {
        return BOT_STATE_DATA_PAIR.stream()
                .map(it -> {
                    try {
                        Integer speakAudioChannelKey = insertAndSelectKeyId(connection, dao.channelKeyTable(), it.getLeft() == null ? null : it.getLeft().getLeft());
                        Integer readTextChannelKey = insertAndSelectKeyId(connection, dao.channelKeyTable(), it.getLeft() == null ? null : it.getLeft().getRight());
                        Integer reconnectSpeakAudioChannelKey = insertAndSelectKeyId(connection, dao.channelKeyTable(), it.getRight() == null ? null : it.getRight().getLeft());
                        Integer reconnectReadTextChannelKey = insertAndSelectKeyId(connection, dao.channelKeyTable(), it.getRight() == null ? null : it.getRight().getRight());
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

        int tableId = retIdAndRecord.get().getId();

        // テーブルIDから取得して確認
        Optional<BotStateDataRecord> retRecordById = dao.botStateDataTable().selectRecordById(connection, tableId);
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

        assertEquals(connectedChannel, dao.botStateDataTable().selectConnectedChannelKeyPair(connection, tableId).orElse(null));
        assertEquals(reconnectConnectedChannel, dao.botStateDataTable().selectReconnectChannelKeyPair(connection, tableId).orElse(null));
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

    // GlobalCustomDictionaryTable

    @Test
    void testGlobalCustomDictionaryTable() throws Exception {
        try (Connection connection = dao.getConnection()) {
            DAO.GlobalCustomDictionaryTable globalCustomDictionaryTable = dao.globalCustomDictionaryTable();

            // テーブルの作成
            globalCustomDictionaryTable.createTableIfNotExists(connection);
            dao.dictionaryReplaceTypeKeyTable().createTableIfNotExists(connection);

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

    // Common

    private <T> Integer insertAndSelectKeyId(Connection connection, DAO.KeyTable<T> table, T key) throws SQLException {
        // キーを追加してIDを取得

        if (key == null) {
            return null;
        }

        table.insertKeyIfNotExists(connection, key);
        return table.selectId(connection, key).orElseThrow();
    }

    private <T, U> Stream<Pair<T, U>> createTestDataStream(Stream<T> data1, Stream<U> data2) {
        // 2個のデータがすべてテストできるストリームを作成

        List<T> data1List = data1.toList();
        List<U> data2List = data2.toList();
        int maxNumOfData = Math.max(data1List.size(), data2List.size());

        return IntStream.range(0, maxNumOfData)
                .mapToObj(i ->
                        Pair.of(data1List.get(i % data1List.size()), data2List.get(i % data2List.size()))
                );
    }

    private Stream<Pair<Long, Long>> createTestDataStream(LongStream data1, LongStream data2) {
        return createTestDataStream(data1.boxed(), data2.boxed());
    }

    private <T, U, V> Stream<Triple<T, U, V>> createTestDataStream(Stream<T> data1, Stream<U> data2, Stream<V> data3) {
        // 3個のデータがすべてテストできるストリームを作成

        List<T> data1List = data1.toList();
        List<U> data2List = data2.toList();
        List<V> data3List = data3.toList();
        int maxNumOfData = Math.max(data1List.size(), Math.max(data2List.size(), data3List.size()));

        return IntStream.range(0, maxNumOfData)
                .mapToObj(i ->
                        Triple.of(data1List.get(i % data1List.size()), data2List.get(i % data2List.size()), data3List.get(i % data3List.size()))
                );
    }

}
