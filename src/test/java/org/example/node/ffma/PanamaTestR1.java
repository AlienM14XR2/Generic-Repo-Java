package org.example.node.ffma;

import java.lang.foreign.MemorySegment;

/**
 * 「Foreign Function & Memory API (Project Panama)」の活用
 * Gradle 9 系は JDK 25 に対応していない。
 * mkdir -p out
 javac -d out -cp out src/test/java/org/example/node/ffma/PanamaBridge.java
 javac -d out -cp out src/test/java/org/example/node/ffma/PanamaTestR1.java
 java --enable-native-access=ALL-UNNAMED \
 -Djava.library.path=/home/jack/dev2/Paradise-Lost/C++/2025/generic_repo_cxx/build \
 -cp out \
 org.example.node.ffma.PanamaTestR1

 *
 */
public class PanamaTestR1 {
    public static void main(String[] args) throws Throwable {
        // 計測開始 (ナノ秒精度)
        long startTime = System.nanoTime();
        try (var bridge = new PanamaBridge()) {
            MemorySegment session = bridge.createSession_MySQL();
            MemorySegment node = bridge.createFulltypeNode("derek@loki.org", "baseball player", 30L);
            bridge.debugNode(node);
            bridge.destroyNode(node);
            bridge.destroySession_MySQL(session);
//            var session = bridge.createSession(...);
//            bridge.beginTx(session);
//            // ここからが純粋な「ネイティブ実行時間」の勝負
//            long id = bridge.insert(session, node);
//            bridge.update(session, node, id);
//            bridge.commitTx(session);

        }
        long endTimeFinal = System.nanoTime();
        double durationAll = (endTimeFinal - startTime) / 1_000_000_000.0;
        System.out.printf("Java (FFM API)               passed %.6f sec.%n", durationAll);
    }
}
