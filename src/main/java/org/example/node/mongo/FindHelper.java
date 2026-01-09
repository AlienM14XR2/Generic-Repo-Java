package org.example.node.mongo;

import org.bson.Document;
import org.bson.types.Binary;
import org.example.node.ValueType;
import org.example.node.VarNode;

import java.time.LocalDateTime;
import java.util.Objects;

public class FindHelper {
    private final VarNode targetNode;

    public FindHelper(VarNode targetNode) {
        this.targetNode = Objects.requireNonNull(targetNode);
    }

    public VarNode toVarNode(Document doc) {
        String pkey = "_id";
        VarNode resultNode = new VarNode("result_node", new ValueType.None(), null);
        resultNode.addChild(pkey, new ValueType.Str(doc.getObjectId(pkey).toHexString()));
        for (VarNode child: targetNode.getChildren()) {
            ValueType data = child.getData();
            switch (data) {
                case ValueType.None none -> resultNode.addChild(child.getKey(), new ValueType.None());
                case ValueType.Int64 v -> {
                    if(doc.getLong(child.getKey()) != null) resultNode.addChild(child.getKey(), new ValueType.Int64(doc.getLong(child.getKey())));
                    else resultNode.addChild(child.getKey(), new ValueType.None());
                }
                case ValueType.Double v -> {
                    if(doc.getDouble(child.getKey()) != null) resultNode.addChild(child.getKey(), new ValueType.Double(doc.getDouble(child.getKey())));
                    else resultNode.addChild(child.getKey(), new ValueType.None());
                }
                case ValueType.Bool v -> {
                    if(doc.getBoolean(child.getKey()) != null) resultNode.addChild(child.getKey(), new ValueType.Bool(doc.getBoolean(child.getKey())));
                    else resultNode.addChild(child.getKey(), new ValueType.None());
                }
                case ValueType.Str v -> {
                    if(doc.getString(child.getKey()) != null) resultNode.addChild(child.getKey(), new ValueType.Str(doc.getString(child.getKey())));
                    else resultNode.addChild(child.getKey(), new ValueType.None());
                }
                case ValueType.DateTime v -> {
                    // タイムゾーンの欠落: MongoDBの BSON Date は内部的に UTC（協定世界時） です。一方でJavaの LocalDateTime はタイムゾーン情報を保持しません。
                    // 暗黙的変換の不確実性: doc.get(key, LocalDateTime.class) を実行する際、ドライバはシステムのデフォルトタイムゾーンを適用して変換を行うため、実行環境（サーバ設定）によって取得される値が変化する不安定要素となります。
                    // java.time.Instant の使用:
                    //BSON Date と 1:1 で対応する（UTCのエポックミリ秒）ため、最も安全です。
                    if(doc.get(child.getKey(), LocalDateTime.class) != null) resultNode.addChild(child.getKey(), new ValueType.DateTime(doc.get(child.getKey(), LocalDateTime.class)));
                    else resultNode.addChild(child.getKey(), new ValueType.None());
                } // java.time.LocalDateTime
                case ValueType.Binary v -> {
                    if(doc.get(child.getKey(), Binary.class) != null) resultNode.addChild(child.getKey(), new ValueType.Binary(doc.get(child.getKey(), Binary.class).getData()));
                    else resultNode.addChild(child.getKey(), new ValueType.None());
                }
            };
        }
        return resultNode;
    }
}
