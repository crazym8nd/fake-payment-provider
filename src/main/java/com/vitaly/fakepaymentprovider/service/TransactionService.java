package com.vitaly.fakepaymentprovider.service;

import com.vitaly.fakepaymentprovider.entity.TransactionEntity;
import com.vitaly.fakepaymentprovider.entity.util.TransactionType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

public interface TransactionService extends GenericService<TransactionEntity, UUID>{
    Flux<TransactionEntity> getAllTransactionsByTypeAndPeriod(TransactionType type, LocalDateTime startDate, LocalDateTime endDate);
    Mono<TransactionEntity> getByIdWithDetails(UUID transactionId);
    Mono<TransactionEntity> processPayoutTransaction(TransactionEntity transactionEntity, String merchantId);
    Mono<TransactionEntity> processTopupTransaction(TransactionEntity transactionEntity);
}
