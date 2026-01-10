package org.example.node.sql.dialect;

import org.example.node.VarNode;

/**
 * RDBMS のSQL方言をサブクラスにて解決するためのインタフェース
 * また、今後発生する可能性がある差異を吸収することも可能（なはず：）。
 *
 */
public interface JdbcStrategy {
    public String insertQuery(final String tableName, final VarNode node);
    public String readQuery(final String tableName, final String primaryKeyName);
    public String updateQuery(final String tableName, final String primaryKeyName, final VarNode node);
    public String deleteQuery(final String tableName, final String primaryKeyName);
}
