package com.vitaly.fakepaymentprovider.service;

import com.vitaly.fakepaymentprovider.entity.TransactionEntity;
import com.vitaly.fakepaymentprovider.entity.util.Status;
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
    Flux<TransactionEntity> getAllTransactionsByTypeAndStatus(TransactionType transactionType, Status status);

    Flux<TransactionEntity> getAllTransactionsForMerchantByTypeAndDay(TransactionType type, LocalDate date, String merchantId);

    Mono<TransactionEntity> getByIdWithDetails(UUID transactionId);
    Mono<Void> processTopTransactionsInProgress(Flux<TransactionEntity> transactions);


    Mono<TransactionEntity> processTransaction(TransactionEntity transactionEntity);
}
