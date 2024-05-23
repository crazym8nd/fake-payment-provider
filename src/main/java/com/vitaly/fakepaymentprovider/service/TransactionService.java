package com.vitaly.fakepaymentprovider.service;

import com.vitaly.fakepaymentprovider.entity.TransactionEntity;
import com.vitaly.fakepaymentprovider.entity.util.TransactionType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public interface TransactionService extends GenericService<TransactionEntity, UUID>{

    Mono<TransactionEntity> validateTopupTransaction(TransactionEntity transactionEntity);

    Mono<TransactionEntity> validatePayoutTransaction(TransactionEntity transactionEntity, String merchantId);

    Flux<TransactionEntity> getAllTransactionsForMerchantByTypeAndPeriod(TransactionType type, LocalDateTime startDate, LocalDateTime endDate, String merchantId);

    Flux<TransactionEntity> getAllTransactionsForMerchantByTypeAndDay(TransactionType type, LocalDate date, String merchantId);

    Mono<TransactionEntity> getByIdWithDetails(UUID transactionId);

    Mono<TransactionEntity> processPayoutTransaction(TransactionEntity transactionEntity, String merchantId);

    Mono<TransactionEntity> processTopupTransaction(TransactionEntity transactionEntity, String merchantId);
}
