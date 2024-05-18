package com.vitaly.fakepaymentprovider.repository;

import com.vitaly.fakepaymentprovider.entity.TransactionEntity;
import com.vitaly.fakepaymentprovider.entity.util.TransactionType;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface TransactionRepository extends R2dbcRepository<TransactionEntity, UUID> {

    @Query("SELECT * FROM transactions WHERE transactions.account_id = :id AND transaction_type = :type AND created_at >= :startDate AND created_at <= :endDate")
    Flux<TransactionEntity> findAllTransactionsForAccountByTypeAndPeriod(TransactionType type, LocalDateTime startDate, LocalDateTime endDate, Long id);

    @Query("SELECT * FROM transactions WHERE transactions.account_id = :id AND transaction_type = :type AND DATE (created_at) = :date")
    Flux<TransactionEntity> findAllTransactionsForAccountByTypeAndDay(TransactionType type, LocalDate date, Long id);

}
