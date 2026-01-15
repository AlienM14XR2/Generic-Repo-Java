package org.example.node.ffma;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

public class PanamaBridge implements AutoCloseable {
    private static final String LIB_PATH = "/home/jack/dev2/Paradise-Lost/C++/2025/generic_repo_cxx/build/libvarnode_bridge.so";
    // 1. static final 化によるリンク・オーバーヘッドの完全排除
    private static final Linker LINKER = Linker.nativeLinker();
    private static final SymbolLookup LIB;
    private static final MethodHandle createNode;
    private static final MethodHandle addChildStr;
    private static final MethodHandle addChildInt64t;
    private static final MethodHandle addChildBool;
    private static final MethodHandle addChildFloat;
    private static final MethodHandle addChildDouble;
    private static final MethodHandle addChildBinary;
    private static final MethodHandle createSessionMySQL;
    private static final MethodHandle debugNode;
    private static final MethodHandle destroyNode;
    private static final MethodHandle destroySessionMySQL;

    private static final MethodHandle insertVarnodeMySQL;
    private static final MethodHandle removeVarnodeMySQL;
    private static final MethodHandle updateVarnodeMySQL;
    private static final MethodHandle findVarnodeMySQL;
    private static final MethodHandle getCStr;
    private static final MethodHandle cstrSize;
    private static final MethodHandle destroyString;
    private static final MethodHandle beginTxMySQL;
    private static final MethodHandle commitTxMySQL;

    static {
        LIB = SymbolLookup.libraryLookup(java.nio.file.Path.of(LIB_PATH), Arena.global());
        createNode = LINKER.downcallHandle(
                LIB.find("create_varnode").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
        );
        addChildStr = LINKER.downcallHandle(
                LIB.find("add_child_str").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
        );
        addChildInt64t = LINKER.downcallHandle(
                LIB.find("add_child_int64_t").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG)
        );
        addChildBool = LINKER.downcallHandle(
                LIB.find("add_child_bool").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_BOOLEAN)
        );
        addChildFloat = LINKER.downcallHandle(
                LIB.find("add_child_float").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_FLOAT)
        );
        addChildDouble = LINKER.downcallHandle(
                LIB.find("add_child_double").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_DOUBLE)
        );
        addChildBinary = LINKER.downcallHandle(
                LIB.find("add_child_binary").orElseThrow(),
                FunctionDescriptor.ofVoid(
                        ValueLayout.ADDRESS,   // node_ptr
                        ValueLayout.ADDRESS,   // key
                        ValueLayout.ADDRESS,   // data (byte*)
                        ValueLayout.JAVA_LONG  // data_len (size_t)
                )
        );
        createSessionMySQL = LINKER.downcallHandle(
                LIB.find("create_session_mysql").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
        );
        debugNode = LINKER.downcallHandle(
                LIB.find("debug_varnode").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
        );
        destroyNode = LINKER.downcallHandle(
                LIB.find("destroy_varnode").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
        );
        destroySessionMySQL = LINKER.downcallHandle(
                LIB.find("destroy_session_mysql").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
        );
        insertVarnodeMySQL = LINKER.downcallHandle(
                LIB.find("insert_varnode_mysql").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
        );
        removeVarnodeMySQL = LINKER.downcallHandle(
                LIB.find("remove_varnode_mysql").orElseThrow(),
                FunctionDescriptor.ofVoid(
                        ValueLayout.ADDRESS,   // 第1引数: void* session_ptr
                        ValueLayout.JAVA_LONG, // 第2引数: int64_t
                        ValueLayout.ADDRESS,   // 第3引数: const char* dbName
                        ValueLayout.ADDRESS    // 第4引数: const char* tableName
                )
        );
        updateVarnodeMySQL = LINKER.downcallHandle(
                LIB.find("update_varnode_mysql").orElseThrow(),
                FunctionDescriptor.ofVoid(
                        ValueLayout.ADDRESS,   // 第1引数: void* session_ptr
                        ValueLayout.ADDRESS,   // 第2引数: void* node_ptr
                        ValueLayout.JAVA_LONG, // 第3引数: int64_t
                        ValueLayout.ADDRESS,   // 第4引数: const char* dbName
                        ValueLayout.ADDRESS    // 第5引数: const char* tableName
                )
        );
        findVarnodeMySQL = LINKER.downcallHandle(
                LIB.find("find_varnode_mysql").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.ADDRESS,   // 戻り値: void*
                        ValueLayout.ADDRESS,   // 第1引数: void* session_ptr
                        ValueLayout.JAVA_LONG, // 第2引数: int64_t
                        ValueLayout.ADDRESS,   // 第3引数: const char* dbName
                        ValueLayout.ADDRESS    // 第4引数: const char* tableName
                )
        );
        getCStr = LINKER.downcallHandle(LIB.find("get_cstr").orElseThrow(), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        cstrSize = LINKER.downcallHandle(LIB.find("cstr_size").orElseThrow(), FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
        destroyString = LINKER.downcallHandle(LIB.find("destroy_string").orElseThrow(), FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
        beginTxMySQL = LINKER.downcallHandle(
                LIB.find("begin_tx_mysql").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
        );
        commitTxMySQL = LINKER.downcallHandle(
                LIB.find("commit_tx_mysql").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
        );
        // ... 残りの MethodHandle も static ブロックで初期化 ...
    }

    private final Arena arena = Arena.ofConfined();
    // 2. 高速化のための「定数セグメント」事前確保
    private final MemorySegment HOST = arena.allocateFrom("localhost");
    private final MemorySegment USER = arena.allocateFrom("root");
    private final MemorySegment PASS = arena.allocateFrom("root1234");
    private final MemorySegment DB_NAME = arena.allocateFrom("test");
    private final MemorySegment TABLE_NAME = arena.allocateFrom("fulltype");

    //
    // 3. 外部公開メソッド
    //

    // MySQL XDEVAPI session の作成。利用後は明示的な廃棄が必要。
    public MemorySegment createSession_MySQL() throws Throwable {
        int PORT = 33060;
        return (MemorySegment)createSessionMySQL.invokeExact(HOST, PORT,USER,PASS);
    }
    public void destroySession_MySQL(MemorySegment session) throws Throwable {
        destroySessionMySQL.invokeExact(session);
    }
    public void destroyNode(MemorySegment node) throws Throwable {
        destroyNode.invokeExact(node);
    }
    public void destroyString(MemorySegment str) throws Throwable {
        destroyString.invokeExact(str);
    }
    public void debugNode(MemorySegment node) throws Throwable {
        debugNode.invokeExact(node);
    }
    public void beginTx_MySQL(MemorySegment session) throws Throwable {
        beginTxMySQL.invokeExact(session);
    }
    public void commitTx_MySQL(MemorySegment session) throws Throwable {
        commitTxMySQL.invokeExact(session);
    }

    // fulltype table data を作成。実体はVarNode。利用後は必ずdestroyメソッドで明示的に破棄すること。
    public MemorySegment createFulltypeNode(
            String email,
            String memo,
            long age
    ) throws Throwable {
        MemorySegment rootKey = arena.allocateFrom("root");
        MemorySegment nodePtr = (MemorySegment)createNode.invokeExact(rootKey);
        addChildStr.invokeExact(nodePtr, arena.allocateFrom("email"), arena.allocateFrom(email));
        addChildStr.invokeExact(nodePtr, arena.allocateFrom("memo"), arena.allocateFrom(memo));
        addChildInt64t.invokeExact(nodePtr, arena.allocateFrom("age"), age);
        addChildBool.invokeExact(nodePtr, arena.allocateFrom("on_busy"), false);
        addChildFloat.invokeExact(nodePtr, arena.allocateFrom("col_float"), 3.14F);
        addChildDouble.invokeExact(nodePtr, arena.allocateFrom("col_double"), 35.6809591);
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
        return nodePtr;
    }
    // fulltype table へのデータ登録。
    public long insertFulltype(MemorySegment session, MemorySegment node) throws Throwable {
        return (long) insertVarnodeMySQL.invokeExact(session, node, DB_NAME, TABLE_NAME);
    }

    @Override
    public void close() throws Exception {
        arena.close();
    }
}
