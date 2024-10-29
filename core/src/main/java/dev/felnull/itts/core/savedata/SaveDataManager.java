package dev.felnull.itts.core.savedata;

import dev.felnull.itts.core.ITTSRuntime;
import dev.felnull.itts.core.savedata.dao.DAO;
import dev.felnull.itts.core.savedata.dao.DAOFactory;
import dev.felnull.itts.core.savedata.legacy.LegacySaveDataLayer;
import dev.felnull.itts.core.savedata.repository.DataRepository;
import dev.felnull.itts.core.savedata.repository.RepoErrorListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * スレッドセーフな保存データの管理<br/>
 *
 * @author MORIMORI0317
 */
public class SaveDataManager {

    /**
     * インスタンス
     */
    private static final SaveDataManager INSTANCE = new SaveDataManager();

    /**
     * ロガー
     */
    private static final Logger LOGGER = LogManager.getLogger(SaveDataManager.class);

    /**
     * SQLiteのDBファイル
     */
    private static final File SQLITE_DB_FILE = new File("./save_data.db");

    /**
     * レポジトリの再作成を行うエラー量
     */
    private static final int REPO_REGEN_NUM_OF_ERROR = 5;

    /**
     * レガシーデータ互換レイヤー
     */
    private final LegacySaveDataLayer legacySaveDataLayer = LegacySaveDataLayer.create(this);

    /**
     * レポジトリ
     */
    private final AtomicReference<DataRepository> repository = new AtomicReference<>();

    /**
     * レポジトリエラー検知用
     */
    private final RepoErrorListener errorListener = this::onRepoError;

    /**
     * エラーのカウンター
     */
    private final AtomicInteger errorCounter = new AtomicInteger();

    /**
     * レポジトリ再作成ロック用オブジェクト
     */
    private final Object repRecreateLock = new Object();

    /**
     * レポジトリ再作成タスク
     */
    private RepoRecreateTask repoRecreateTask;

    /**
     * コンストラクタ
     */
    private SaveDataManager() {
    }

    public static SaveDataManager getInstance() {
        return INSTANCE;
    }

    /**
     * 初期化<br/>
     * 必ずコンフィグが読み込まれた後に呼び出してください。
     */
    public void init() {
        DataRepository repo = DataRepository.create(createDAO());
        repo.init();
        repo.addErrorListener(errorListener);
        repository.set(repo);
    }

    private DAO createDAO() {
        return DAOFactory.getInstance().createSQLiteDAO(SQLITE_DB_FILE);
    }


    public LegacySaveDataLayer getLegacySaveDataLayer() {
        return legacySaveDataLayer;
    }

    /**
     * レポジトリを取得
     *
     * @return データレポジトリのインスタンス
     */
    @NotNull
    public DataRepository getRepository() {
        DataRepository repo = repository.get();

        if (repo == null) {
            tryRepRecreate();
            throw new RuntimeException("The repository is currently being prepared");
        }

        return repo;
    }

    private void onRepoError() {
        if (errorCounter.incrementAndGet() >= REPO_REGEN_NUM_OF_ERROR) {
            tryRepRecreate();
        }
    }

    private void tryRepRecreate() {
        synchronized (repRecreateLock) {
            if (repoRecreateTask == null) {
                // タスクが存在しない場合は作成して開始
                repoRecreateTask = new RepoRecreateTask();
                repoRecreateTask.start();
            }
        }
    }

    private void finishRepRecreate() {
        synchronized (repRecreateLock) {
            if (repoRecreateTask != null) {
                // タスクが存在すれば破棄する
                repoRecreateTask = null;
            }
        }
    }

    /**
     * レポジトリ作成タスク
     */
    private final class RepoRecreateTask {

        private void start() {
            try {
                // 既存のレポジトリを廃棄
                DataRepository repo = repository.getAndSet(null);
                if (repo != null) {
                    repo.removeErrorListener(errorListener);
                    repo.dispose();
                }

                // 非同期でレポジトリを作成する
                CompletableFuture.supplyAsync(() -> {
                    DataRepository ret = DataRepository.create(createDAO());
                    ret.init();
                    ret.addErrorListener(errorListener);
                    return ret;
                }, ITTSRuntime.getInstance().getAsyncWorkerExecutor()).whenCompleteAsync((dataRepository, throwable) -> {

                    if (throwable == null) {
                        repository.set(dataRepository);
                        LOGGER.info("Repository was reloaded");
                    } else {
                        LOGGER.error("Failed to load repository", throwable);
                    }

                    finishRepRecreate();
                }, ITTSRuntime.getInstance().getAsyncWorkerExecutor());

            } catch (Throwable throwable) {
                LOGGER.error("Repository recreation task failed to start");
                finishRepRecreate();
            }

        }
    }
}
