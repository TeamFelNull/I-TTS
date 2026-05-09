package dev.felnull.itts.core.statistics.repository;

import dev.felnull.itts.core.statistics.dao.StatisticsDAO;
import dev.felnull.itts.core.statistics.dao.StatisticsDAO.TTSCountSum;
import dev.felnull.itts.core.statistics.repository.impl.StatisticsRepositoryImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;

/**
 * 統計データを管理するレポジトリ
 * 一部を除きスレッドセーフ
 */
public interface StatisticsRepository {

    /**
     * インスタンスを作成
     *
     * @param dao 初期化前のDAO
     * @return レポジトリインスタンス
     */
    static StatisticsRepository create(@NotNull StatisticsDAO dao) {
        return new StatisticsRepositoryImpl(dao);
    }

    /**
     * 初期化
     */
    void init();

    /**
     * 破棄
     */
    void dispose();

    /**
     * エラーリスナーを追加
     * スレッドセーフではない
     *
     * @param listener リスナー
     */
    void addErrorListener(@NotNull StatisticsRepoErrorListener listener);

    /**
     * エラーリスナーを削除
     * スレッドセーフではない
     *
     * @param listener リスナー
     */
    void removeErrorListener(@NotNull StatisticsRepoErrorListener listener);

    /**
     * 読み上げカウントを増分する
     *
     * @param botId           BOTのDiscord ID
     * @param serverId        サーバーのDiscord ID
     * @param voiceTypeId     ボイスタイプID 不明な場合はnull
     * @param voiceCategoryId ボイスカテゴリID 不明な場合はnull
     * @param date            集計日
     * @param charDelta       文字数の増分
     * @param messageDelta    メッセージ数の増分
     */
    void increment(long botId,
                   long serverId,
                   @Nullable String voiceTypeId,
                   @Nullable String voiceCategoryId,
                   @NotNull LocalDate date,
                   long charDelta,
                   long messageDelta);

    /**
     * 集計を取得する
     * サーバーや期間にnullを渡すとそのフィルタを無視する
     *
     * @param botId    BOTのDiscord ID
     * @param serverId サーバーのDiscord IDフィルタ nullで全サーバー集計
     * @param from     開始日 nullで下限なし
     * @param to       終了日 nullで上限なし
     * @return 文字数とメッセージ数の合計
     */
    @NotNull
    TTSCountSum sumCount(long botId,
                         @Nullable Long serverId,
                         @Nullable LocalDate from,
                         @Nullable LocalDate to);
}
