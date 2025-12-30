package org.example.node;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TestVarNode {
    private static VarNode root;

    @BeforeEach
    void beforeEach() {
        root = new VarNode("root", new ValueType.None(), null);
    }

    @Test
    public void test_addChild_normal() throws Exception {
        System.out.println("------ test_addChild_normal");
        // 同じKey 異なる型の追加
        root.addChild("number", new ValueType.Int64(300));
        VarNode child_str = root.addChild("str", new ValueType.Str("foo"));
        child_str.addChild("dbl", new ValueType.Double(3.141592));
        // check
        VarNode.debug(root, 0);
        Optional<VarNode> actual_str_a = root.getChild("str");
        Assertions.assertTrue(actual_str_a.isPresent());
        Double actual_dbl = actual_str_a.get().getChild("dbl")
                .map(c -> c.get(Double.class))
                .orElse(null);
        System.out.println("actual_dbl: "+actual_dbl);
        assertThat(actual_dbl, is(3.141592));
        Long actual_n = root.getChild("number")
                .map(c -> c.get(Long.class))
                .orElse(null);
        String actual_str = root.getChild("str")
                .map(c -> c.get(String.class))
                .orElse(null);
        System.out.println("actual_n: "+actual_n);
        System.out.println("actual_str: "+actual_str);
        System.out.println("size: "+root.getChildren().size());
        assertThat(actual_n, is(300L));
        assertThat(actual_str, is("foo"));
        assertThat(root.getChildren().size(), is(2));
    }

    @Test
    public void test_addChild_overwritten() throws Exception {
        System.out.println("------ test_addChild_overwritten");
        // 同じKey 異なる型の追加
        String key = "key1";
        String expect = "over-written";
        root.addChild(key, new ValueType.Int64(300));
        root.addChild(key, new ValueType.Str(expect));
        // check
        String actual = root.getChild(key)
                .map(c -> c.get(String.class))
                .orElse(null);
        System.out.println("actual: "+actual);
        System.out.println("size: "+root.getChildren().size());
        assertThat(actual, is(expect));
        assertThat(root.getChildren().size(), is(1));
    }
}
