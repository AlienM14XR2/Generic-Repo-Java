package org.example.node.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.BsonObjectId;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.example.node.Repository;
import org.example.node.ValueType;
import org.example.node.VarNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;

public class VarNodeRepository implements Repository<String, VarNode> {
    private final MongoDatabase db;
    private final String collectionName;
    private final FindHelper findHelper;

    private static Document getDocument(VarNode varNode) {
        Document doc = new Document();
        for (VarNode child: varNode.getChildren()) {
            ValueType data = child.getData();
            Object value = switch (data) {
                case ValueType.None none    -> null; // C++: mysqlx::Value() (NULL)
                case ValueType.Int64 v      -> v.value();
                case ValueType.Double v     -> v.value();
                case ValueType.Bool v       -> v.value();
                case ValueType.Str v        -> v.value();
                case ValueType.DateTime v   -> v.value(); // java.time.LocalDateTime
                case ValueType.Binary v     -> v.value();
            };
            doc.append(child.getKey(), value);
        }
        return doc;
    }

    public VarNodeRepository(MongoDatabase db,
                             String collectionName)
    {
        this.db = Objects.requireNonNull(db);
        this.collectionName = Objects.requireNonNull(collectionName);
        this.findHelper = null;
    }

    public VarNodeRepository(MongoDatabase db,
                             String collectionName,
                             FindHelper findHelper)
    {
        this.db = Objects.requireNonNull(db);
        this.collectionName = Objects.requireNonNull(collectionName);
        this.findHelper = Objects.requireNonNull(findHelper);;
    }

    @Override
    public String insert(VarNode varNode) {
        MongoCollection<Document> collection = db.getCollection(collectionName);
        Document doc = getDocument(varNode);
        InsertOneResult result = collection.insertOne(doc);
        System.out.println("inserted ID: " + result.getInsertedId());
        BsonObjectId insertedId = Objects.requireNonNull(result.getInsertedId()).asObjectId();
        return insertedId.getValue().toHexString();
//        return ((BsonObjectId) Objects.requireNonNull(result.getInsertedId())).getValue().toString();
    }

    @Override
    public void update(String id, VarNode varNode) {
        MongoCollection<Document> collection = db.getCollection(collectionName);
        ObjectId objectId = new ObjectId(id);
        Bson filter = eq("_id", objectId);
        List<Bson> updates = new ArrayList<>();
        for(VarNode child: varNode.getChildren()) {
            Bson update = getBson(child);
            updates.add(update);
        }
        UpdateResult result = collection.updateOne(filter, updates);
        System.out.println("matched count: "+ result.getMatchedCount());
        System.out.println("modified count: "+ result.getModifiedCount());
    }

    private static Bson getBson(VarNode child) {
        ValueType data = child.getData();
        Object value = switch (data) {
            case ValueType.None none    -> null; // C++: mysqlx::Value() (NULL)
            case ValueType.Int64 v      -> v.value();
            case ValueType.Double v     -> v.value();
            case ValueType.Bool v       -> v.value();
            case ValueType.Str v        -> v.value();
            case ValueType.DateTime v   -> v.value(); // java.time.LocalDateTime
            case ValueType.Binary v     -> v.value();
        };
        return Updates.set(child.getKey(), value);
    }

    @Override
    public void remove(String id) {
        ObjectId objectId = new ObjectId(id);
        Bson filter = eq("_id", objectId);
        MongoCollection<Document> collection = db.getCollection(collectionName);
        DeleteResult result = collection.deleteOne(filter);
        System.out.println("removed count: " + result.getDeletedCount());
    }

    @Override
    public Optional<VarNode> findById(String id) {
        MongoCollection<Document> collection = db.getCollection(collectionName);
        Document doc = collection.find(eq("_id", new ObjectId(id))).first();
        if( doc == null || doc.isEmpty()) {
            return Optional.empty();
        }
        VarNode resultNode = Objects.requireNonNull(findHelper).toVarNode(doc);
        return Optional.of(resultNode);
    }
}
