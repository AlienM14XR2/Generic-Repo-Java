package org.example;
import org.example.node.ValueType;
import org.example.node.VarNode;
//TIP コードを<b>実行</b>するには、<shortcut actionId="Run"/> を押すか
// ガターの <icon src="AllIcons.Actions.Execute"/> アイコンをクリックします。
public class Main {
    private static int test_VarNode() {
        System.out.println("------ test_VarNode");
        try {
            ValueType.Str t_name = new ValueType.Str("contractor");
            VarNode root = new VarNode("table", t_name, null);
            root.addChild("company_id", new ValueType.Str("D1_1200"));
            root.addChild("email", new ValueType.Str("derek@loki.org"));
            root.addChild("password", new ValueType.Str("derek1111"));
            root.addChild("name", new ValueType.Str("DEREK"));
            VarNode.debug(root,0);
            return 0;
        } catch(Exception e) {
            System.out.println(e.getMessage());
            return 1;
        }
    }

    public static void main(String[] args) {
        System.out.printf("Play and Result ... %d", test_VarNode());
    }
}