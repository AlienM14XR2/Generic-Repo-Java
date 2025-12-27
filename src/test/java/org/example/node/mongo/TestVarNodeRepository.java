package org.example.node.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.example.node.Repository;
import org.example.node.ValueType;
import org.example.node.VarNode;
import org.junit.jupiter.api.*;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestVarNodeRepository {
    private static final String uri = "mongodb://user:pass@localhost:27017";
    private static final MongoClient client = MongoClients.create(uri);
    private static final String dbName = "USER";
    private static final String collectionName = "contractor";
    private static String id;

    @BeforeAll
    static void beforeAll() {
        System.out.println("------ beforeAll");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("------ afterAll");
        client.close();
    }

    @Test
    @Order(1)
    public void testInsert_mongo() throws Exception {
        System.out.println("------ testInsert_mongo");
        VarNode data = new VarNode("contractor", new ValueType.None(), null);
        data.addChild("company_id", new ValueType.Str("D1_1200"));
        data.addChild("email",      new ValueType.Str("derek@loki.org"));
        data.addChild("password",   new ValueType.Str("derek1111"));
        data.addChild("name",       new ValueType.Str("DEREK"));
        Repository<String, VarNode> repo = new VarNodeRepository(client.getDatabase(dbName), collectionName);
        String actual = repo.insert(data);
        System.out.println("id: "+actual);
        Assertions.assertNotNull(actual);
        id = actual;
    }

    @Test
    @Order(2)
    public void testUpdate_mongo() throws Exception {
        System.out.println("------ testUpdate_mongo");
        VarNode targetNode = new VarNode("contractor", new ValueType.None(), null);
        targetNode.addChild("company_id", new ValueType.Str(""));
        targetNode.addChild("email",      new ValueType.Str(""));
        targetNode.addChild("password",   new ValueType.Str(""));
        targetNode.addChild("name",       new ValueType.Str(""));
        targetNode.addChild("roles",       new ValueType.Str(""));

        VarNode data = new VarNode("contractor", new ValueType.None(), null);
        String expectEmail = "derek_u@loki.org";
        String expectName = "DEREK_U";
        String expectRoles = "Admin,User";
        data.addChild("company_id", new ValueType.Str("D1_3333"));
        data.addChild("email",      new ValueType.Str(expectEmail));
        data.addChild("password",   new ValueType.Str("derek_u1111"));
        data.addChild("name",       new ValueType.Str(expectName));
        data.addChild("roles",      new ValueType.Str(expectRoles));
        Repository<String, VarNode> repo = new VarNodeRepository(client.getDatabase(dbName), collectionName, new FindHelper(targetNode));
        repo.update(id, data);
        Optional<VarNode> result = repo.findById(id);
        result.ifPresent(node -> {
            VarNode.debug(node, 0); // 目視用
            String actualEmail = node.getChild("email")
                    .map(c -> c.get(String.class))
                    .orElse(null);
            String actualName = node.getChild("name")
                    .map(c -> c.get(String.class))
                    .orElse(null);
            String actualRoles = node.getChild("roles")
                    .map(c -> c.get(String.class))
                    .orElse(null);
            assertThat(actualEmail, is(expectEmail));
            assertThat(actualName, is(expectName));
            assertThat(actualRoles, is(expectRoles));
        });
    }

    @Test
    @Order(3)
    public void testFindById_mongo() throws Exception {
        VarNode targetNode = new VarNode("contractor", new ValueType.None(), null);
        targetNode.addChild("company_id", new ValueType.Str(""));
        targetNode.addChild("email",      new ValueType.Str(""));
        targetNode.addChild("password",   new ValueType.Str(""));
        targetNode.addChild("name",       new ValueType.Str(""));
        targetNode.addChild("roles",       new ValueType.Str(""));
        Repository<String, VarNode> repo = new VarNodeRepository(client.getDatabase(dbName), collectionName, new FindHelper(targetNode));
        Optional<VarNode> result = repo.findById(id);
        if(result.isPresent()) {
            VarNode.debug(result.get(),0);
        } else {
            throw new RuntimeException("Unexpected case.");
        }
    }

    @Test
    @Order(4)
    public void testRemove_mongo() throws Exception {
        Repository<String, VarNode> repo = new VarNodeRepository(client.getDatabase(dbName), collectionName);
        repo.remove(id);
    }
}
