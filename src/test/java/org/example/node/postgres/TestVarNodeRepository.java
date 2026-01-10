package org.example.node.postgres;

import org.example.node.Repository;
import org.example.node.ValueType;
import org.example.node.VarNode;
import org.example.node.jdbc.VarNodeRepository;
import org.example.node.sql.dialect.PostgresStrategy;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestVarNodeRepository {
    private static final String URL = "jdbc:postgresql://localhost:5432/derek";
    private static final String USER = "derek";
    private static final String PASS = "derek1234";
    private static final String tableName = "contractor";
    private static final String primaryKeyName = "id";
    private static Connection conn;
    private static Long id = null;

    @BeforeAll
    static void beforeAll() throws SQLException {
        System.out.println("------ beforeAll");
        conn = DriverManager.getConnection(URL, USER, PASS);
        conn.setAutoCommit(false);
    }

    @AfterAll
    static void afterAll() throws SQLException {
        System.out.println("------ afterAll");
//        conn.rollback();
        conn.commit();
        conn.close();
    }

    @Test
    @Order(1)
    void testInsert_postgres() throws Exception {
        System.out.println("------ testInsert_postgres");
        VarNode data = new VarNode("contractor", new ValueType.None(), null);
        data.addChild("company_id", new ValueType.Str("D1_1200"));
        data.addChild("email",      new ValueType.Str("derek@loki.org"));
        data.addChild("password",   new ValueType.Str("derek1111"));
        data.addChild("name",       new ValueType.Str("DEREK"));
        Repository<Long, VarNode> repo = new VarNodeRepository(conn, tableName, new PostgresStrategy());
        Long actual = null;
        actual = repo.insert(data);
        Assertions.assertNotNull(actual);
        assertThat(actual, is(greaterThan(0L)));
        id = actual;
    }

    @Test
    @Order(2)
    void testFindById_postgres() throws Exception {
        System.out.println("------ testFindById_postgres");
        Repository<Long, VarNode> repo = new VarNodeRepository(conn, tableName, primaryKeyName, new PostgresStrategy());
        Optional<VarNode> actual = repo.findById(id);
        if(actual.isPresent()) {
            VarNode.debug(actual.get(), 0);
        } else {
            throw new RuntimeException("Unexpected case.");
        }
    }

    @Test
    @Order(3)
    void testUpdate_postgres() throws Exception {
        System.out.println("------ testUpdate_postgres");
        String expectEmail = "updated_derek@loki.org";
        String expectName  = "UPDATED_DEREK";
        VarNode data = new VarNode("contractor", new ValueType.None(), null);
        data.addChild("company_id", new ValueType.Str("D1_1234"));
        data.addChild("email",      new ValueType.Str(expectEmail));
        data.addChild("password",   new ValueType.Str("derek1111_u"));
        data.addChild("name",       new ValueType.Str(expectName));
        data.addChild("roles",       new ValueType.Str("Admin,User"));
        Repository<Long, VarNode> repo = new VarNodeRepository(conn, tableName, primaryKeyName, new PostgresStrategy());
        repo.update(id, data);
        // 検証
        Optional<VarNode> actual = repo.findById(id);
        Assertions.assertTrue(actual.isPresent(), "レコードが存在しません");
        actual.ifPresent(node -> {
            VarNode.debug(node, 0); // 目視用
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
    @Order(4)
    void testRemove_postgres() throws Exception {
        System.out.println("------ testRemove_postgres");
        Repository<Long, VarNode> repo = new VarNodeRepository(conn, tableName, primaryKeyName, new PostgresStrategy());
        repo.remove(id);
        Optional<VarNode> actual = repo.findById(id);
        Assertions.assertTrue(actual.isEmpty());
    }
}
