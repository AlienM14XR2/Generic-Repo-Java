package org.example.node.ffma;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * 「Foreign Function & Memory API (Project Panama)」の活用
 * Gradle 9 系は JDK 25 に対応していない。
 * mkdir -p out
 javac -d out src/test/java/org/example/node/ffma/PanamaTest.java
 * # 実行
 * # --enable-native-access: FFM APIの使用を許可
 * # -Djava.library.path: .so が置かれているディレクトリを指定
 java --enable-native-access=ALL-UNNAMED \
  -Djava.library.path=/home/jack/dev2/Paradise-Lost/C++/2025/generic_repo_cxx/build \
  -cp out \
  org.example.node.ffma.PanamaTest
 *
 */
public class PanamaTest {
    private static final String varnodeBridgeModulePath = "/home/jack/dev2/Paradise-Lost/C++/2025/generic_repo_cxx/build/libvarnode_bridge.so";
    public static void main(String[] args) throws Throwable {
        // 計測開始 (ナノ秒精度)
        long startTime = System.nanoTime();

        Linker linker = Linker.nativeLinker();
        // ビルドした .so ファイルをロード
        SymbolLookup lib = SymbolLookup.libraryLookup(Path.of(varnodeBridgeModulePath), Arena.global());

        // 1. C++ 関数の定義 (MethodHandle)
        MethodHandle createNode = linker.downcallHandle(
                lib.find("create_varnode").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
        );

        MethodHandle addChildStr = linker.downcallHandle(
                lib.find("add_child_str").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
        );

        MethodHandle addChildInt64t = linker.downcallHandle(
                lib.find("add_child_int64_t").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG)
        );

        MethodHandle addChildBool = linker.downcallHandle(
                lib.find("add_child_bool").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_BOOLEAN)
        );

        MethodHandle addChildFloat = linker.downcallHandle(
                lib.find("add_child_float").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_FLOAT)
        );

        MethodHandle addChildDouble = linker.downcallHandle(
                lib.find("add_child_double").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_DOUBLE)
        );

        MethodHandle addChildBinary = linker.downcallHandle(
                lib.find("add_child_binary").orElseThrow(),
                FunctionDescriptor.ofVoid(
                        ValueLayout.ADDRESS,   // node_ptr
                        ValueLayout.ADDRESS,   // key
                        ValueLayout.ADDRESS,   // data (byte*)
                        ValueLayout.JAVA_LONG  // data_len (size_t)
                )
        );

        MethodHandle createSessionMysql = linker.downcallHandle(
                lib.find("create_session_mysql").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
        );

        MethodHandle debugNode = linker.downcallHandle(
                lib.find("debug_varnode").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
        );

        MethodHandle destroyNode = linker.downcallHandle(
                lib.find("destroy_varnode").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
        );

        MethodHandle destroySessionMySQL = linker.downcallHandle(
                lib.find("destroy_session_mysql").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
        );

        MethodHandle insertVarnodeMysql = linker.downcallHandle(
                lib.find("insert_varnode_mysql").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.JAVA_LONG, // 戻り値: int64_t
                        ValueLayout.ADDRESS,   // 第1引数: void* session_ptr
                        ValueLayout.ADDRESS,   // 第2引数: void* node_ptr
                        ValueLayout.ADDRESS,   // 第3引数: const char* dbName
                        ValueLayout.ADDRESS    // 第4引数: const char* tableName
                )
        );

        MethodHandle removeVarnodeMysql = linker.downcallHandle(
                lib.find("remove_varnode_mysql").orElseThrow(),
                FunctionDescriptor.ofVoid(
                        ValueLayout.ADDRESS,   // 第1引数: void* session_ptr
                        ValueLayout.JAVA_LONG, // 第2引数: int64_t
                        ValueLayout.ADDRESS,   // 第3引数: const char* dbName
                        ValueLayout.ADDRESS    // 第4引数: const char* tableName
                )
        );

        MethodHandle updateVarnodeMysql = linker.downcallHandle(
                lib.find("update_varnode_mysql").orElseThrow(),
                FunctionDescriptor.ofVoid(
                        ValueLayout.ADDRESS,   // 第1引数: void* session_ptr
                        ValueLayout.ADDRESS,   // 第2引数: void* node_ptr
                        ValueLayout.JAVA_LONG, // 第3引数: int64_t
                        ValueLayout.ADDRESS,   // 第4引数: const char* dbName
                        ValueLayout.ADDRESS    // 第5引数: const char* tableName
                )
        );

        MethodHandle findVarnodeMysql = linker.downcallHandle(
                lib.find("find_varnode_mysql").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.ADDRESS,   // 戻り値: void*
                        ValueLayout.ADDRESS,   // 第1引数: void* session_ptr
                        ValueLayout.JAVA_LONG, // 第2引数: int64_t
                        ValueLayout.ADDRESS,   // 第3引数: const char* dbName
                        ValueLayout.ADDRESS    // 第4引数: const char* tableName
                )
        );

        MethodHandle cstrSize = linker.downcallHandle(
                lib.find("cstr_size").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.JAVA_LONG, // 戻り値: size_t
                        ValueLayout.ADDRESS    // 第1引数: void* str_ptr
                )
        );

        MethodHandle destroyString = linker.downcallHandle(
                lib.find("destroy_string").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
        );

        MethodHandle getCStr = linker.downcallHandle(
                lib.find("get_cstr").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.ADDRESS,    // 戻値: const char*
                        ValueLayout.ADDRESS     // 第1引数: void* str_ptr
                )
        );

        MethodHandle beginTxMysql = linker.downcallHandle(
                lib.find("begin_tx_mysql").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
        );

        MethodHandle commitTxMysql = linker.downcallHandle(
                lib.find("commit_tx_mysql").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
        );

        try (Arena arena = Arena.ofConfined()) {
            // 2. C++ インスタンスの生成 (create_varnode("root"))
            MemorySegment rootKey = arena.allocateFrom("root");
            MemorySegment nodePtr = (MemorySegment) createNode.invokeExact(rootKey);
            MemorySegment host = arena.allocateFrom("localhost");
            int port = 33060;
            MemorySegment user = arena.allocateFrom("root");
            MemorySegment pass = arena.allocateFrom("root1234");
            MemorySegment session = (MemorySegment) createSessionMysql.invokeExact(host,port,user,pass);
            // 3. 子要素の追加 (add_child_str(nodePtr, "status", "success"))
            addChildStr.invokeExact(nodePtr, arena.allocateFrom("email"), arena.allocateFrom("derek@loki.org"));
            addChildStr.invokeExact(nodePtr, arena.allocateFrom("memo"), arena.allocateFrom("baseball player"));
            addChildInt64t.invoke(nodePtr, arena.allocateFrom("age"), 30L);
            addChildBool.invoke(nodePtr, arena.allocateFrom("on_busy"), Boolean.TRUE);
            addChildFloat.invoke(nodePtr, arena.allocateFrom("col_float"), 3.14F);
            addChildDouble.invoke(nodePtr, arena.allocateFrom("col_double"), 35.6809591);
            byte[] bin = {'a', '1', 'b', '2'};
            // byte配列を直接メモリセグメントに割り当て
            MemorySegment binSegment = arena.allocateFrom(ValueLayout.JAVA_BYTE, bin);
            // データ本体のポインタと、そのサイズ (bin.length) を渡す
            addChildBinary.invokeExact(
                    nodePtr,
                    arena.allocateFrom("raw_data"),
                    binSegment,
                    (long) bin.length
            );

            // 4. C++ 側のデバッグ出力を実行
            System.out.println("--- C++ Debug Output Start ---");
            debugNode.invokeExact(nodePtr);
            System.out.println("--- C++ Debug Output End ---");

            // Tx 開始
            beginTxMysql.invokeExact(session);
            long endTime1 = System.nanoTime();
            // 5. Insert 実行
            long insertedId = (long) insertVarnodeMysql.invokeExact(
                    session,                       // すでに生成済みの session ポインタ
                    nodePtr,                       // すでに生成済みの node ポインタ
                    arena.allocateFrom("test"), // dbName
                    arena.allocateFrom("fulltype") // tableName
            );
            long endTime2 = System.nanoTime();
            System.out.println("insertId: "+insertedId);
            // 6. Update 実行
            long endTime3 = System.nanoTime();
            MemorySegment nodePtr_u = (MemorySegment)createNode.invokeExact(rootKey);
            addChildStr.invokeExact(nodePtr_u, arena.allocateFrom("email"), arena.allocateFrom("derek_update@loki.org"));
            addChildStr.invokeExact(nodePtr_u, arena.allocateFrom("memo"), arena.allocateFrom("football player, PG"));
            addChildInt64t.invoke(nodePtr_u, arena.allocateFrom("age"), 33L);
            updateVarnodeMysql.invokeExact(
                    session,
                    nodePtr_u,
                    insertedId,
                    arena.allocateFrom("test"),
                    arena.allocateFrom("fulltype")
            );
            long endTime4 = System.nanoTime();
            // 7. Find 実行
            MemorySegment findResult = (MemorySegment)findVarnodeMysql.invokeExact(
                    session,
                    insertedId,
                    arena.allocateFrom("test"),
                    arena.allocateFrom("fulltype")
            );
            MemorySegment cstr = (MemorySegment)getCStr.invokeExact(findResult);
            long resultSize = (long)cstrSize.invokeExact(findResult);
            System.out.println("resultSize: "+resultSize);
            MemorySegment sizedSegment = cstr.reinterpret(resultSize+1);
            System.out.println("sizedSegment: "+sizedSegment.getString(0L, StandardCharsets.UTF_8));
            long endTime5 = System.nanoTime();

            // 8. Remove 実行
            removeVarnodeMysql.invokeExact(
                    session,
                    insertedId,
                    arena.allocateFrom("test"),
                    arena.allocateFrom("fulltype")
            );
            long endTime6 = System.nanoTime();
            // tx コミット
            commitTxMysql.invokeExact(session);

            // 9. 解放
            destroyString.invokeExact(findResult);
            // 現状、Inser やUpdate を正しく完了した際は、明示的な開放の必要がない（行うとむしろエラーになる）。
//            destroyNode.invokeExact(nodePtr);
            destroySessionMySQL.invokeExact(session);
            long endTime7 = System.nanoTime();
            // 秒単位に変換して出力 (C++側の CLOCKS_PER_SEC と比較用)
            double durationIns = (endTime2 - endTime1) / 1_000_000_000.0;
            double durationUpd = (endTime4 - endTime3) / 1_000_000_000.0;
            double durationFin = (endTime5 - endTime4) / 1_000_000_000.0;
            double durationDel = (endTime6 - endTime5) / 1_000_000_000.0;
            double durationAll = (endTime7 - startTime) / 1_000_000_000.0;
            System.out.printf("Java (FFM API) insertId: %d, passed %.6f sec.%n", insertedId, durationIns);
            System.out.printf("Java (FFM API) updateId: %d, passed %.6f sec.%n", insertedId, durationUpd);
            System.out.printf("Java (FFM API) findId:   %d, passed %.6f sec.%n", insertedId, durationFin);
            System.out.printf("Java (FFM API) removeId: %d, passed %.6f sec.%n", insertedId, durationDel);
            System.out.printf("Java (FFM API)               passed %.6f sec.%n", durationAll);
        }
    }
}
