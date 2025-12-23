package org.example.node;

import java.util.Optional;

/**
 * リポジトリパターンの基底インターフェース
 * @param <ID> 識別子の型
 * @param <Data>  扱うデータの型
 */
public interface Repository<ID, Data> {
    ID insert(Data data);
    void update(ID id, Data data);
    void remove(ID id);
    Optional<Data> findById(ID id);
}

