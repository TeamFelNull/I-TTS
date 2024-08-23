package dev.felnull.itts.core.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * データベースのコンフィグ
 */
public interface DataBaseConfig {

    /**
     * デフォルトのデータベースの種類
     */
    DataBaseType DEFAULT_TYPE = DataBaseType.SQLITE;

    /**
     * デフォルトのホスト名
     */
    String DEFAULT_HOST = "";

    /**
     * デフォルトのポート番号
     */
    int DEFAULT_PORT = 0;

    /**
     * デフォルトのデータベース名
     */
    String DEFAULT_DATABASE_NAME = "";

    /**
     * デフォルトのユーザー名
     */
    String DEFAULT_USER = "";

    /**
     * デフォルトのパスワード
     */
    String DEFAULT_PASSWORD = "";

    /**
     * データベースの種類
     *
     * @return 種類
     */
    @NotNull
    DataBaseType getType();

    /**
     * SQLのホスト名
     *
     * @return ホスト名
     */
    @NotNull
    String getHost();

    /**
     * SQLのポート番号
     *
     * @return ポート番号
     */
    @Range(from = 0, to = 65535)
    int getPort();

    /**
     * SQLのデータベース名
     *
     * @return データベース名
     */
    @NotNull
    String getDatabaseName();

    /**
     * SQLのユーザー名
     *
     * @return ユーザー名
     */
    @NotNull
    String getUser();

    /**
     * SQLのパスワード
     *
     * @return パスワード
     */
    @NotNull
    String getPassword();

    /**
     * SQLの種類
     */
    enum DataBaseType {
        SQLITE("sqlite"),
        MYSQL("mysql");

        /**
         * 名前
         */
        private final String name;

        DataBaseType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

}
