package com.vitaly.fakepaymentprovider.repository;

import com.vitaly.fakepaymentprovider.entity.TransactionEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface TransactionRepository extends R2dbcRepository<TransactionEntity, String> {
    @Modifying
    @Query("UPDATE transactions SET status = 'DELETED' WHERE id = :id")
    Mono<Void> deleteById(String id);
}
