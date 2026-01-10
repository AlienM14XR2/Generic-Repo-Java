package org.example.node.sql.dialect;

import org.example.node.VarNode;
import java.util.List;
import java.util.StringJoiner;

public class MySqlStrategy implements JdbcStrategy {

    @Override
    public String insertQuery(final String tableName, final VarNode node) {
        List<VarNode> children = node.getChildren();
        if (children.isEmpty()) {
            throw new IllegalArgumentException("Node must have at least one child to generate INSERT query.");
        }
        StringBuilder sql = new StringBuilder();
        // MySQL 用にバッククォートでエスケープ
        sql.append("INSERT INTO `").append(tableName).append("` (");
        StringBuilder cols = new StringBuilder();
        StringBuilder vals = new StringBuilder();
        int i = 0;
        for (VarNode child : children) {
            if (i > 0) {
                cols.append(",");
                vals.append(",");
            }
            cols.append("`").append(child.getKey()).append("`");
            vals.append("?");
            ++i;
        }
        sql.append(cols).append(") VALUES (").append(vals).append(")");
        return sql.toString();
    }

    @Override
    public String readQuery(final String tableName, final String primaryKeyName) {
        if (tableName == null || primaryKeyName == null) {
            throw new IllegalArgumentException("Table name and Primary Key name must not be null.");
        }
        return String.format("SELECT * FROM `%s` WHERE `%s`=?", tableName, primaryKeyName);
    }

    @Override
    public String updateQuery(final String tableName, final String primaryKeyName, final VarNode node) {
        List<VarNode> children = node.getChildren();
        if (children.isEmpty()) {
            throw new IllegalArgumentException("Update node must have at least one child.");
        }
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE `").append(tableName).append("` SET ");
        StringJoiner setClause = new StringJoiner(", ");
        for (VarNode child : children) {
            setClause.add("`" + child.getKey() + "`=?");
        }
        sql.append(setClause.toString());
        sql.append(" WHERE `").append(primaryKeyName).append("`=?");
        return sql.toString();
    }

    @Override
    public String deleteQuery(final String tableName, final String primaryKeyName) {
        if (tableName == null || primaryKeyName == null) {
            throw new IllegalArgumentException("Table name and Primary Key name must not be null.");
        }
        return String.format("DELETE FROM `%s` WHERE `%s`=?", tableName, primaryKeyName);
    }
}
