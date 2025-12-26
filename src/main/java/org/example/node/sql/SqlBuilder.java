package org.example.node.sql;

import org.example.node.VarNode;

import java.util.List;
import java.util.StringJoiner;

public class SqlBuilder {

    public static String readQuery(final String tableName, final String primaryKeyName) {
        // e.g. SELECT * FROM mytable WHERE columnfoo = ?
        if (tableName == null || primaryKeyName == null) {
            throw new IllegalArgumentException("Table name and Primary Key name must not be null.");
        }
        return String.format("SELECT * FROM \"%s\" WHERE \"%s\"=?", tableName, primaryKeyName);
    }

    /**
     * VarNode の構造に基づき、パラメータ化された INSERT 文（SQLテンプレート）を生成する。
     * 仕様：テーブル名は関数の引数とする（C++ でもVarNode のルートは利用していない、特定の意味をもたせていない）。
     */
    public static String insertQuery(final String tableName, final VarNode node) {
        // 実装例: node の子要素からカラム名を抽出し、
        // "INSERT INTO ... (?, ?, ...)" を組み立てる処理
        // e.g. INSERT INTO sample_table (col_int, col_double, col_bool, col_str, col_datetime) VALUES (?, ?, ?, ?, ?)

        // node が null tableName が null 等、挙げればきりがないので、この関数ではそれらは考慮しない。
        // この例外は、一見正しくSQL が発行されたかのような勘違いを未然に防ぐ程度のものと考えること。
        List<VarNode> children = node.getChildren();
        if (children.isEmpty()) {
            throw new IllegalArgumentException("Node must have at least one child to generate INSERT query.");
        }
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(tableName).append(" (");
        StringBuilder cols = new StringBuilder();
        StringBuilder vals = new StringBuilder();
        int i = 0;
        for(VarNode child : node.getChildren()) {
            if(i == 0) {
                cols.append("\"").append(child.getKey()).append("\"");
                vals.append("?");
            } else {
                cols.append(",").append("\"").append(child.getKey()).append("\"");
                vals.append(",?");
            }
            ++i;
        }
        sql.append(cols).append(") VALUES (").append(vals).append(")");
        return sql.toString();
    }

    /**
     * @param tableName      テーブル名
     * @param primaryKeyName プライマリキーのカラム名
     * @param node           更新対象のカラムのみを含む VarNode (主キーは含まない)
     */
    public static String updateQuery(final String tableName, final String primaryKeyName, final VarNode node) {
        List<VarNode> children = node.getChildren();
        if (children.isEmpty()) {
            throw new IllegalArgumentException("Update node must have at least one child.");
        }
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE \"").append(tableName).append("\"\nSET ");
        // 1. SET 句の構築 (例: "col1"=?, "col2"=?)
        StringJoiner setClause = new StringJoiner(", ");
        for (VarNode child : children) {
            setClause.add("\"" + child.getKey() + "\"=?");
        }
        sql.append(setClause.toString());
        // 2. WHERE 句の構築 (例: WHERE "id"=?)
        sql.append("\nWHERE \"").append(primaryKeyName).append("\"=?");
        return sql.toString();
    }

    /**
     * 指定されたプライマリキーに基づく DELETE 文を生成する。
     */
    public static String deleteQuery(final String tableName, final String primaryKeyName) {
        if (tableName == null || primaryKeyName == null) {
            throw new IllegalArgumentException("Table name and Primary Key name must not be null.");
        }
        return String.format("DELETE FROM \"%s\" WHERE \"%s\"=?", tableName, primaryKeyName);
    }
}
