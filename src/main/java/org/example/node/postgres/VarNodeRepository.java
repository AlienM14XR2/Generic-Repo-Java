package org.example.node.postgres;

import org.example.node.Repository;
import org.example.node.ValueType;
import org.example.node.VarNode;
import org.example.node.sql.SqlBuilder;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class VarNodeRepository implements Repository<Long, VarNode> {
    private final Connection conn;
    private final String tableName;
    private final String primaryKeyName;

    private int setPreparedStatement(PreparedStatement pstmt, VarNode varNode) throws SQLException {
        int i = 1;
        for (VarNode child : varNode.getChildren()) {
            ValueType data = child.getData();
            // 明示的な型指定と LocalDateTime の setObject 指定
            switch (data) {
                case ValueType.None none    -> pstmt.setNull(i, Types.NULL);
                case ValueType.Int64 v      -> pstmt.setLong(i, v.value());
                case ValueType.Double v     -> pstmt.setDouble(i, v.value());
                case ValueType.Bool v       -> pstmt.setBoolean(i, v.value());
                case ValueType.Str v        -> pstmt.setString(i, v.value());
                case ValueType.DateTime v   -> pstmt.setObject(i, v.value()); // JDBC 4.2+ (LocalDateTime)
                case ValueType.Binary v     -> pstmt.setBytes(i, v.value());
            }
            ++i;
        }
        return i;
    }

    /**
     * コンストラクタ
     *
     */
    VarNodeRepository(
            Connection conn,
            String tableName
    ) {
        this.conn = Objects.requireNonNull(conn);
        this.tableName = Objects.requireNonNull(tableName);
        this.primaryKeyName = null;
    }

    VarNodeRepository(
            Connection conn,
            String tableName,
            String primaryKeyName
    ) {
        this.conn = Objects.requireNonNull(conn);
        this.tableName = Objects.requireNonNull(tableName);
        this.primaryKeyName = Objects.requireNonNull(primaryKeyName);
    }

    @Override
    public Long insert(VarNode varNode) {
        // DONE... VarNode を利用したSQL の動的作成
        String sql = SqlBuilder.insertQuery(tableName, varNode);
        System.out.println("sql: "+sql);
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // DONE... VarNode を利用したPreparedStatement の設定
            setPreparedStatement(pstmt, varNode);
            pstmt.executeUpdate();
            // 2. 生成されたキーを取得
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long id = generatedKeys.getLong(1); // 生成されたIDを取得
                    System.out.println("生成されたID: " + id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public void update(Long id, VarNode varNode) {
        String sql = SqlBuilder.updateQuery(tableName, primaryKeyName, varNode);
        System.out.println("sql: "+sql);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // DONE... VarNode を利用したPreparedStatement の設定
            int nextIndex = setPreparedStatement(pstmt, varNode);
            pstmt.setLong(nextIndex, id); // 最後の ? (WHERE句) に ID をセット
            int affectedRows = pstmt.executeUpdate();
            System.out.println("更新件数: " + affectedRows);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove(Long id) {
        final String sql = SqlBuilder.deleteQuery(tableName, primaryKeyName);
        System.out.println("sql: " + sql);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            int affectedRows = pstmt.executeUpdate();
            System.out.println("削除件数: " + affectedRows);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to remove record with ID: " + id, e);
        }
    }

    private void mappedVarNodeByRS(VarNode resultNode, ResultSet rs, ResultSetMetaData rsmd) throws SQLException {
        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
            int type = rsmd.getColumnType(i);
            ValueType data = switch (type) {
                case Types.BIGINT, Types.INTEGER -> {
                    long val = rs.getLong(i);
                    yield rs.wasNull() ? new ValueType.None() : new ValueType.Int64(val);
                }
                case Types.DOUBLE, Types.FLOAT -> {
                    double val = rs.getDouble(i);
                    yield rs.wasNull() ? new ValueType.None() : new ValueType.Double(val);
                }
                case Types.VARCHAR, Types.CHAR -> {
                    String val = rs.getString(i);
                    yield (val == null) ? new ValueType.None() : new ValueType.Str(val);
                }
                case Types.BOOLEAN, Types.BIT -> {
                    boolean val = rs.getBoolean(i);
                    yield rs.wasNull() ? new ValueType.None() : new ValueType.Bool(val);
                }
                case Types.TIMESTAMP, Types.TIMESTAMP_WITH_TIMEZONE -> {
                    LocalDateTime val = rs.getObject(i, LocalDateTime.class);
                    yield (val == null) ? new ValueType.None() : new ValueType.DateTime(val);
                }
                case Types.BINARY, Types.VARBINARY, Types.BLOB -> {
                    byte[] val = rs.getBytes(i);
                    yield (val == null) ? new ValueType.None() : new ValueType.Binary(val);
                }
                default -> throw new IllegalStateException("Unexpected Type value: " + type);
            };
            resultNode.addChild(rsmd.getColumnLabel(i),data);
        }
    }

    @Override
    public Optional<VarNode> findById(Long id) {
        String sql = SqlBuilder.readQuery(tableName, primaryKeyName);
        System.out.println("sql: " + sql);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            VarNode resultNode = new VarNode("result_node", new ValueType.None(), null);
            if(rs.next()) {
                mappedVarNodeByRS(resultNode, rs, rsmd);
                return Optional.of(resultNode);
            } else {
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find record with ID: " + id, e);
        }
    }
}
