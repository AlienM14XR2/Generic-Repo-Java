package org.exapmle.node.mysql;


import com.mysql.cj.xdevapi.Session;
import com.mysql.cj.xdevapi.SessionFactory;
import org.example.node.ValueType;
import org.example.node.VarNode;
import org.example.node.mysql.VarNodeRepository;
import org.junit.jupiter.api.*;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.OptionalMatchers.isPresent; // 値が存在することを確認
//import static org.hamcrest.OptionalMatchers.hasValue;  // 特定の値を確認

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestVarNodeRepository {
    private static final String dbName = "test";
    private static final String tableName = "contractor";
    private static final String primaryKeyName = "id";
    private static Session mySession = null;
    // 本来は DbUnit を用いるべきだが、最速の検証方法を選んだ。
    private static Long id = null;
//    private static Long id = 714L;

    @BeforeAll
    static void beforeAll() {
        System.out.println("------ beforeAll");
        mySession = new SessionFactory().getSession("mysqlx://localhost:33060/" + dbName + "?user=root&password=root1234");
        mySession.startTransaction();
    }

    @AfterAll
    static void afterAll() {
        System.out.println("------ afterAll");
        mySession.rollback();
        mySession.close();
    }

    @Test
    @Order(4)
    void testRemove()
    {
        System.out.println("------ testRemove");
        VarNodeRepository<Long> repo = new VarNodeRepository<>(mySession, dbName, tableName, primaryKeyName);
        repo.remove(id);
        Optional<VarNode> actual = repo.findById(id);
        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @Order(3)
    void testUpdate() {
        System.out.println("------ testUpdate");
        VarNodeRepository<Long> repo = new VarNodeRepository<>(mySession, dbName, tableName, primaryKeyName);

        String expectEmail = "updated_derek@loki.org";
        String expectName  = "UPDATED_DEREK";

        VarNode updateData = new VarNode("contractor", new ValueType.None(), null);
        updateData.addChild("email", new ValueType.Str(expectEmail));
        updateData.addChild("name",  new ValueType.Str(expectName));

        repo.update(id, updateData);

        // 検証
        Optional<VarNode> actual = repo.findById(id);
        Assertions.assertTrue(actual.isPresent(), "レコードが存在しません");

        actual.ifPresent(node -> {
            VarNode.debug(node, 0); // 目視用

            // 冗長さを排除した値の抽出と検証
            String actualEmail = node.getChild("email")
                    .map(c -> c.get(String.class))
                    .orElse(null);

            String actualName = node.getChild("name")
                    .map(c -> c.get(String.class))
                    .orElse(null);

            assertThat(actualEmail, is(expectEmail));
            assertThat(actualName, is(expectName));
        });
    }

    @Test
    @Order(2)
    void testFindById() {
        System.out.println("------ testFindById");
        VarNodeRepository<Long> repo = new VarNodeRepository<>(mySession, dbName, tableName, primaryKeyName);
        Optional<VarNode> actual = repo.findById(id);
//        assertThat(actual.isPresent(), is(equals(true)));
        Assertions.assertTrue(actual.isPresent());
        actual.ifPresent(result -> VarNode.debug(result, 0));
    }

    @Test
    @Order(1)
    void testInsert() {
        System.out.println("------ testInsert");
        VarNode data = new VarNode("contractor", new ValueType.None(), null);
        data.addChild("company_id", new ValueType.Str("D1_1200"));
        data.addChild("email",      new ValueType.Str("derek@loki.org"));
        data.addChild("password",   new ValueType.Str("derek1111"));
        data.addChild("name",       new ValueType.Str("DEREK"));
        VarNodeRepository<Long> repo = new VarNodeRepository<>(mySession, dbName, tableName, primaryKeyName);
        Long actual = null;
        actual = repo.insert(data);
        System.out.println("id is " + actual);
        Assertions.assertNotNull(actual);
        assertThat(actual, is(greaterThan(0L)));
        id = actual;
    }
}
