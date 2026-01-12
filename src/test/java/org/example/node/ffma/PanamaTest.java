package org.example.node.ffma;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * 「Foreign Function & Memory API (Project Panama)」の活用
 * Gradle 9 系は JDK 25 に対応していない。
 * mkdir -p out
 * javac -d out src/test/java/org/example/node/ffma/PanamaTest.java
 * # 実行
 * # --enable-native-access: FFM APIの使用を許可
 * # -Djava.library.path: .so が置かれているディレクトリを指定
 * java --enable-native-access=ALL-UNNAMED \
 *      -Djava.library.path=/home/jack/dev2/Paradise-Lost/C++/2025/generic_repo_cxx/build \
 *      -cp out \
 *      org.example.node.ffma.PanamaTest
 *
 */
public class PanamaTest {
    private static final String varnodeBridgeModulePath = "/home/jack/dev2/Paradise-Lost/C++/2025/generic_repo_cxx/build/libvarnode_bridge.so";
    public static void main(String[] args) throws Throwable {
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
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
        );

        MethodHandle debugNode = linker.downcallHandle(
                lib.find("debug_varnode").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
        );

        MethodHandle destroyNode = linker.downcallHandle(
                lib.find("destroy_varnode").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
        );

        try (Arena arena = Arena.ofConfined()) {
            // 2. C++ インスタンスの生成 (create_varnode("root"))
            MemorySegment rootKey = arena.allocateFrom("root");
            MemorySegment nodePtr = (MemorySegment) createNode.invokeExact(rootKey);

            // 3. 子要素の追加 (add_child_str(nodePtr, "status", "success"))
            addChildStr.invokeExact(nodePtr, arena.allocateFrom("email"), arena.allocateFrom("derek@loki.org"));
            addChildStr.invokeExact(nodePtr, arena.allocateFrom("memo"), arena.allocateFrom("baseball player"));
            addChildInt64t.invoke(nodePtr, arena.allocateFrom("age"), 30L);
            addChildBool.invoke(nodePtr, arena.allocateFrom("on_busy"), Boolean.TRUE);
            addChildFloat.invoke(nodePtr, arena.allocateFrom("col_float"), 3.14F);
            addChildDouble.invoke(nodePtr, arena.allocateFrom("col_double"), 35.6809591);
            byte[] bin = {'a', '1', 'b', '2'};
            addChildBinary.invoke(nodePtr, arena.allocateFrom("raw_data"), arena.allocateFrom(Arrays.toString(bin)));

            // 4. C++ 側のデバッグ出力を実行
            System.out.println("--- C++ Debug Output Start ---");
            debugNode.invokeExact(nodePtr);
            System.out.println("--- C++ Debug Output End ---");

            // 5. 解放
            destroyNode.invokeExact(nodePtr);
        }
    }
}
