package org.example.node.mysql;

import org.example.node.Repository;
import org.example.node.ValueType;
import org.example.node.VarNode;
import com.mysql.cj.xdevapi.*;

import java.util.List;
import java.util.Objects;

/**
 * C++: class VarNodeRepository : public tmp::Repository<ID, tmp::VarNode>
 */
public class VarNodeRepository<ID> implements Repository<ID, VarNode> {

    // C++: mysqlx::Session* const session;
    private final Session session;

    private final String dbName;
    private final String tableName;
    private final String primaryKeyName;
    /**
     * C++: std::vector<mysqlx::Value> convertVarNodeToSqlValues(const tmp::VarNode& data_tree)
     * Java 21 の switch パターンマッチングを使用して値を抽出する
     */
    private List<Object> convertVarNodeToSqlValues(VarNode dataTree) {
        List<Object> valuesToBind = new java.util.ArrayList<>();

        for (VarNode child : dataTree.getChildren()) {
            ValueType data = child.getData();

            // C++: std::visit と if constexpr による型判定を再現
            Object sqlValue = switch (data) {
                case ValueType.None none    -> null; // C++: mysqlx::Value() (NULL)
                case ValueType.Int64 v      -> v.value();
                case ValueType.Double v     -> v.value();
                case ValueType.Bool v       -> v.value();
                case ValueType.Str v        -> v.value();
                case ValueType.DateTime v   -> v.value(); // java.time.LocalDateTime
            };
            valuesToBind.add(sqlValue);
        }
        return valuesToBind;
    }

    /**
     * コンストラクタ
     *
     */
    public VarNodeRepository(
            Session session,
            String db,
            String table,
            String primaryKeyName
    ) {
        this.session = Objects.requireNonNull(session);
        this.dbName = Objects.requireNonNull(db);
        this.tableName = Objects.requireNonNull(table);
        this.primaryKeyName = Objects.requireNonNull(primaryKeyName);
    }

    // デフォルト引数 "id" を考慮したオーバーロード
    public VarNodeRepository(Session session, String db, String table) {
        this(session, db, table, "id");
    }

    @Override
    @SuppressWarnings("unchecked")
    public ID insert(VarNode data) {
        // C++: abi::__cxa_demangle(typeid(*this).name(),...)
        System.out.println("insert ... " + this.getClass().getSimpleName());

        // 子要素（カラム名）のリストを作成
        // C++: data.children[i]->key
        List<String> cols = data.getChildren().stream()
                .map(VarNode::getKey)
                .toList();

        // 値の変換（オリジナル関数 convertVarNodeToSqlValues のシグネチャを想定）
        // C++: convertVarNodeToSqlValues(data)
        List<Object> vals = convertVarNodeToSqlValues(data);

        Schema db = session.getSchema(dbName);
        Table table = db.getTable(tableName);

        // X DevAPI Java: values() は配列または可変引数を受け取る
        InsertResult res = table.insert(cols.toArray(new String[0]))
                .values(vals.toArray())
                .execute();

        // 実行結果のログ
        System.out.println("affected items count: "+res.getAffectedItemsCount());

        // オートインクリメント値を ID 型にキャストして返却
        return (ID) res.getAutoIncrementValue();
    }

    @Override
    public void update(ID id, VarNode data) {
        // C++: print_debug -> System.out.println
        System.out.println("update ... " + this.getClass().getSimpleName());

        Schema db = session.getSchema(dbName);
        Table table = db.getTable(tableName);

        // 1. まず update ステートメントを開始する
        // C++: auto tableUpdate = table.update();
        UpdateStatement tableUpdate = table.update();

        // 2. VarNode の子ノード（更新対象カラム）をループ処理し、動的に set() を呼び出す
        for (VarNode child : data.getChildren()) {
            ValueType valueData = child.getData();

            // C++: std::visit と if constexpr (!std::is_same_v<T, std::monostate>) を再現
            // ValueType.None 以外の場合に set を実行する
            switch (valueData) {
                case ValueType.None none -> { /* 何もしない */ }
                case ValueType.Int64 v    -> tableUpdate.set(child.getKey(), v.value());
                case ValueType.Double v   -> tableUpdate.set(child.getKey(), v.value());
                case ValueType.Bool v     -> tableUpdate.set(child.getKey(), v.value());
                case ValueType.Str v      -> tableUpdate.set(child.getKey(), v.value());
                case ValueType.DateTime v -> tableUpdate.set(child.getKey(), v.value());
            };
        }

        // 3. WHERE句を設定して実行する
        String condition = primaryKeyName + " = :id";
        Result res = tableUpdate.where(condition)
                .bind("id", id)
                .execute();

        System.out.println("affected items count: " + res.getAffectedItemsCount());
    }

    @Override
    public java.util.Optional<VarNode> findById(ID id) {
        // C++: print_debug -> System.out.println に置き換え
        System.out.println("findById ... " + this.getClass().getSimpleName());
        Schema db = session.getSchema(dbName);
        Table table = db.getTable(tableName);
        String condition = primaryKeyName + " = :id";
        RowResult rows = table.select()
                .where(condition)
                .bind("id", id)
                .execute();

        // C++: rows.count() == 0
        if (!rows.hasNext()) {
            return java.util.Optional.empty();
        }
        // C++: rows.fetchOne()
        Row row = rows.next();
        List<Column> meta = rows.getColumns();
        // 以前記録した VarNode のコンストラクタ（key, data, parent）に従う
        // C++: tmp::VarNode result_entity("result_row", std::monostate{});
        VarNode resultEntity = new VarNode("result_row", new ValueType.None(), null);
        // Java では List なので標準的な for ループとインデックスを使用
        for (int i = 0; i < meta.size(); i++) {
            Column colMeta = meta.get(i);
            String colName = colMeta.getColumnName();
            // 行オブジェクトからインデックスで値を取得
            // Java X DevAPI では Object が返却される
            // C++: convertSqlValueToVarNode_Corrected
            Type mysqlType = colMeta.getType();
            ValueType result = switch (mysqlType) {
                case BIGINT, INT, SMALLINT, TINYINT ->
                        new ValueType.Int64(row.getLong(i));
                case FLOAT, DOUBLE, DECIMAL ->
                        new ValueType.Double(row.getDouble(i));
                case STRING, JSON ->
                        new ValueType.Str(row.getString(i));
                case DATETIME, TIMESTAMP ->
                    // Timestamp から LocalDateTime への変換が必要
                    // AI 推奨の次のメソッドは存在しない。
                    // new ValueType.DateTime(row.getLocalDateTime(i));
                    new ValueType.DateTime(row.getTimestamp(i).toLocalDateTime());
                case BIT ->
                        new ValueType.Bool(row.getBoolean(i));
                default ->
                        new ValueType.None();
            };
            resultEntity.addChild(colName, result);
        }
        return java.util.Optional.of(resultEntity);
    }

    @Override
    public void remove(ID id) {
        // C++: print_debug -> System.out.println
        System.out.println("remove ... " + this.getClass().getSimpleName());
        Schema db = session.getSchema(dbName);
        Table table = db.getTable(tableName);
        String condition = primaryKeyName + " = :id";
        // C++: table.remove()
        // Java X DevAPI: table.delete() が C++ の remove() に相当する実在メソッド
        Result res = table.delete()
                .where(condition)
                .bind("id", id)
                .execute();
        System.out.println("affected items count: " + res.getAffectedItemsCount());
    }

}
