package org.example.node.mysql;

import org.example.node.Repository;
import org.example.node.ValueType;
import org.example.node.VarNode;
import org.example.node.jdbc.VarNodeRepository;
import org.example.node.sql.dialect.MySqlStrategy; // MySql用Strategyを注入
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestVarNodeRepositoryMySql {
    // MySQL 接続情報 (環境に合わせて適宜修正)
    private static final String URL = "jdbc:mysql://localhost:3306/test";
    private static final String USER = "root";
    private static final String PASS = "root1234";
    private static final String tableName = "contractor";
    private static final String primaryKeyName = "id";
    private static Connection conn;
    private static Long id = null;

    @BeforeAll
    static void beforeAll() throws SQLException {
        System.out.println("------ beforeAll (MySQL)");
        conn = DriverManager.getConnection(URL, USER, PASS);
        conn.setAutoCommit(false);
    }

    @AfterAll
    static void afterAll() throws SQLException {
        System.out.println("------ afterAll (MySQL)");
        if (conn != null) {
            conn.commit();
            conn.close();
        }
    }

    @Test
    @Order(1)
    void testInsert_mysql() throws Exception {
        System.out.println("------ testInsert_mysql");
        VarNode data = new VarNode("contractor", new ValueType.None(), null);
        data.addChild("company_id", new ValueType.Str("D1_1200"));
        data.addChild("email",      new ValueType.Str("derek@loki.org"));
        data.addChild("password",   new ValueType.Str("derek1111"));
        data.addChild("name",       new ValueType.Str("DEREK"));

        // MySqlStrategy を注入
        Repository<Long, VarNode> repo = new VarNodeRepository(conn, tableName, new MySqlStrategy());
        Long actual = repo.insert(data);

        Assertions.assertNotNull(actual);
        assertThat(actual, is(greaterThan(0L)));
        id = actual;
    }

    @Test
    @Order(2)
    void testFindById_mysql() throws Exception {
        System.out.println("------ testFindById_mysql");
        Repository<Long, VarNode> repo = new VarNodeRepository(conn, tableName, primaryKeyName, new MySqlStrategy());
        Optional<VarNode> actual = repo.findById(id);
        if(actual.isPresent()) {
            VarNode.debug(actual.get(), 0);
        } else {
            throw new RuntimeException("Unexpected case: Record not found for ID " + id);
        }
    }

    @Test
    @Order(3)
    void testUpdate_mysql() throws Exception {
        System.out.println("------ testUpdate_mysql");
        String expectEmail = "updated_derek@mysql.org";
        String expectName  = "UPDATED_MYSQL";
        VarNode data = new VarNode("contractor", new ValueType.None(), null);
        data.addChild("company_id", new ValueType.Str("D1_9999"));
        data.addChild("email",      new ValueType.Str(expectEmail));
        data.addChild("password",   new ValueType.Str("mysql_pass"));
        data.addChild("name",       new ValueType.Str(expectName));

        Repository<Long, VarNode> repo = new VarNodeRepository(conn, tableName, primaryKeyName, new MySqlStrategy());
        repo.update(id, data);

        // 検証
        Optional<VarNode> actual = repo.findById(id);
        Assertions.assertTrue(actual.isPresent(), "レコードが存在しません");
        actual.ifPresent(node -> {
            VarNode.debug(node, 0);
            String actualEmail = node.getChild("email").map(c -> c.get(String.class)).orElse(null);
            String actualName = node.getChild("name").map(c -> c.get(String.class)).orElse(null);
            assertThat(actualEmail, is(expectEmail));
            assertThat(actualName, is(expectName));
        });
    }

    @Test
    @Order(4)
    void testRemove_mysql() throws Exception {
        System.out.println("------ testRemove_mysql");
        Repository<Long, VarNode> repo = new VarNodeRepository(conn, tableName, primaryKeyName, new MySqlStrategy());
        repo.remove(id);
        Optional<VarNode> actual = repo.findById(id);
        Assertions.assertTrue(actual.isEmpty());
    }
}
