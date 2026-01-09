package org.example.node;

import java.time.LocalDateTime;
import java.util.*;


public final class VarNode {
    private final String key;
    private ValueType data;
    private final VarNode parent;
    private final List<VarNode> children = new ArrayList<>();

    // コンストラクタ
    public VarNode(String key, ValueType data, VarNode parent) {
        this.key = Objects.requireNonNull(key);
        this.data = Objects.requireNonNull(data);
        this.parent = parent;
    }

    // 子要素の追加
    public VarNode addChild(String key, ValueType data) {
        Optional<VarNode> existing = getChild(key);
        if (existing.isPresent()) {
            existing.get().data = data;
            return existing.get();
        }
        VarNode child = new VarNode(key, data, this);
        this.children.add(child);
        return child;
    }

    /**
     * 値の取得 (std::get<T> の代替)
     * Java 17 の instanceof パターンマッチングにより、キャストの手間を省きつつ型を抽出
     */
    public <T> T get(Class<T> clazz) {
        if (data instanceof ValueType.Int64(long value) && clazz == Long.class) return clazz.cast(value);
        if (data instanceof ValueType.Str(String value) && clazz == String.class) return clazz.cast(value);
        if (data instanceof ValueType.Bool(boolean value) && clazz == Boolean.class) return clazz.cast(value);
        if (data instanceof ValueType.Double(double value) && clazz == Double.class) return clazz.cast(value);
        if (data instanceof ValueType.DateTime(LocalDateTime value) && clazz == LocalDateTime.class) return clazz.cast(value);
        throw new ClassCastException("Type mismatch or unsupported type: " + data.getClass().getName());
    }

    public boolean isType(Class<? extends ValueType> clazz) {
        return clazz.isInstance(this.data);
    }

    // 特定のキーを持つ子要素の取得 (nullptr返却の代わりに Optional を使用)
    public Optional<VarNode> getChild(String key) {
        return children.stream()
                .filter(c -> c.key.equals(key))
                .findFirst();
    }

    // 読み取り専用のゲッター
    public String getKey() { return key; }
    public ValueType getData() { return data; }
    public List<VarNode> getChildren() {
        return Collections.unmodifiableList(children);
    }
    // debug 出力
    public static void debug(VarNode varNode, int indent) {
        System.out.print("  ".repeat(Math.max(0, indent * 2)) +"key: " + varNode.key);
        // switch式でのパターンマッチングにより、std::visitのように型に応じた処理を記述
        switch (varNode.data) {
            case ValueType.None none    -> System.out.println("\tdata: null");
            case ValueType.Int64 int64  -> System.out.println("\tdata: " + int64);
            case ValueType.Double d     -> System.out.println("\tdata: " + d);
            case ValueType.Bool   b     -> System.out.println("\tdata: " + b);
            case ValueType.Str    str   -> System.out.println("\tdata: " + str);
            case ValueType.DateTime dt  -> System.out.println("\tdata: " + dt);
            case ValueType.Binary bin   -> System.out.println("\tdata: " + bin);
        };
        // sealed class のため、コンパイラが全てのケースが網羅されていることを確認できる
        for(VarNode child: varNode.children) {
            debug(child, indent+1);
        }
    }
}
