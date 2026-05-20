package dev.felnull.itts.core.statistics;

import dev.felnull.itts.core.ITTSRuntime;
import dev.felnull.itts.core.config.DataBaseConfig;
import dev.felnull.itts.core.statistics.dao.StatisticsDAO;
import dev.felnull.itts.core.statistics.dao.StatisticsDAOFactory;
import dev.felnull.itts.core.statistics.repository.StatisticsRepoErrorListener;
import dev.felnull.itts.core.statistics.repository.StatisticsRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 統計機能のライフサイクル管理
 * スレッドセーフ
 */
public final class StatisticsManager {

    /**
     * インスタンス
     */
    private static final StatisticsManager INSTANCE = new StatisticsManager();

    /**
     * ロガー
     */
    private static final Logger LOGGER = LogManager.getLogger(StatisticsManager.class);

    /**
     * SQLiteのDBファイル
     */
    private static final File SQLITE_DB_FILE = new File("./statistics_data.db");

    /**
     * レポジトリの再作成を行うエラー量
     */
    private static final int REPO_REGEN_NUM_OF_ERROR = 5;

    /**
     * レポジトリ
     */
    private final AtomicReference<StatisticsRepository> repository = new AtomicReference<>();

    /**
     * レポジトリエラー検知用
     */
    private final StatisticsRepoErrorListener errorListener = this::onRepoError;

    /**
     * エラーのカウンター
     */
    private final AtomicInteger errorCounter = new AtomicInteger();

    /**
     * レポジトリ再作成ロック用オブジェクト
     */
    private final Object repRecreateLock = new Object();

    /**
     * 機能有効フラグ
     */
    private volatile boolean enabled;

    /**
     * 統計データベース設定
     */
    private volatile DataBaseConfig dataBaseConfig;

    /**
     * レポジトリ再作成タスク
     */
    private RepoRecreateTask repoRecreateTask;

    /**
     * コンストラクタ
     */
    private StatisticsManager() {
    }

    /**
     * インスタンスを取得
     *
     * @return マネージャ
     */
    public static StatisticsManager getInstance() {
        return INSTANCE;
    }

    /**
     * 初期化
     * 必ずコンフィグが読み込まれた後に呼び出す
     */
    public void init() {
        StatisticsConfig statisticsConfig = ITTSRuntime.getInstance().getConfigManager().getConfig().getStatisticsConfig();
        this.enabled = statisticsConfig.isEnable();
        this.dataBaseConfig = statisticsConfig.getDataBase();

        if (!enabled) {
            LOGGER.info("Statistics feature is disabled");
            return;
        }

        StatisticsRepository repo = StatisticsRepository.create(createDAO());
        repo.init();
        repo.addErrorListener(errorListener);
        repository.set(repo);
    }

    /**
     * 破棄
     */
    public void dispose() {
        StatisticsRepository repo = repository.getAndSet(null);
        if (repo != null) {
            repo.removeErrorListener(errorListener);
            repo.dispose();
        }
    }

    /**
     * 機能が有効かどうかを取得
     *
     * @return 有効ならtrue
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * レポジトリを取得
     * 統計機能が無効な場合や準備中はnullを返す
     *
     * @return レポジトリ
     */
    @Nullable
    public StatisticsRepository getRepository() {
        if (!enabled) {
            return null;
        }
        StatisticsRepository repo = repository.get();
        if (repo == null) {
            tryRepRecreate();
        }
        return repo;
    }

    private StatisticsDAO createDAO() {
        StatisticsDAOFactory factory = StatisticsDAOFactory.getInstance();
        return switch (dataBaseConfig.getType()) {
            case SQLITE -> factory.createSQLiteDAO(SQLITE_DB_FILE);
            case MYSQL -> factory.createMysqlDAO(
                    dataBaseConfig.getHost(),
                    dataBaseConfig.getPort(),
                    dataBaseConfig.getDatabaseName(),
                    dataBaseConfig.getUser(),
                    dataBaseConfig.getPassword()
            );
        };
    }

    private void onRepoError(Throwable throwable) {
        if (errorCounter.incrementAndGet() >= REPO_REGEN_NUM_OF_ERROR) {
            tryRepRecreate();
        }
    }

    private void tryRepRecreate() {
        synchronized (repRecreateLock) {
            if (repoRecreateTask == null) {
                repoRecreateTask = new RepoRecreateTask();
                repoRecreateTask.start();
            }
        }
    }

    private void finishRepRecreate() {
        synchronized (repRecreateLock) {
            if (repoRecreateTask != null) {
                repoRecreateTask = null;
            }
        }
    }

    /**
     * レポジトリ再作成タスク
     */
    private final class RepoRecreateTask {

        private void start() {
            try {
                StatisticsRepository repo = repository.getAndSet(null);
                if (repo != null) {
                    repo.removeErrorListener(errorListener);
                    repo.dispose();
                }

                CompletableFuture.supplyAsync(() -> {
                    StatisticsRepository ret = StatisticsRepository.create(createDAO());
                    ret.init();
                    ret.addErrorListener(errorListener);
                    return ret;
                }, ITTSRuntime.getInstance().getAsyncWorkerExecutor()).whenCompleteAsync((statisticsRepository, throwable) -> {
                    if (throwable == null) {
                        repository.set(statisticsRepository);
                        errorCounter.set(0);
                        LOGGER.info("Statistics repository was reloaded");
                    } else {
                        LOGGER.error("Failed to load statistics repository", throwable);
                    }
                    finishRepRecreate();
                }, ITTSRuntime.getInstance().getAsyncWorkerExecutor());
            } catch (Throwable throwable) {
                LOGGER.error("Statistics repository recreation task failed to start", throwable);
                finishRepRecreate();
            }
        }
    }
}
