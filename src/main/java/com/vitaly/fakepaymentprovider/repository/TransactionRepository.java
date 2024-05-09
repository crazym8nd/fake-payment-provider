package com.vitaly.fakepaymentprovider.repository;

import com.vitaly.fakepaymentprovider.entity.TransactionEntity;
import com.vitaly.fakepaymentprovider.entity.util.TransactionType;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface TransactionRepository extends R2dbcRepository<TransactionEntity, UUID> {
    @Modifying
    @Query("UPDATE transactions SET status = 'DELETED' WHERE transaction_id = :id")
    Mono<Void> deleteById(UUID id);

    @Query("SELECT * FROM transactions WHERE transaction_type = :type AND created_at >= :startDate AND created_at <= :endDate")
    Flux<TransactionEntity> findTopUpTransactions(TransactionType type, LocalDateTime startDate, LocalDateTime endDate);


    @Query("SELECT * FROM transactions WHERE transaction_type = :type AND DATE(created_at) = :date")
    Flux<TransactionEntity> findTransactionsByTypeAndDay(TransactionType type, LocalDate date);

}
