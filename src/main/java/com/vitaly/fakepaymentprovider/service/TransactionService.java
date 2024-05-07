package com.vitaly.fakepaymentprovider.service;

import com.vitaly.fakepaymentprovider.entity.TransactionEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

public interface TransactionService extends GenericService<TransactionEntity, UUID>{
    Flux<TransactionEntity> getAllByPeriod(LocalDateTime startDateTime, LocalDateTime endDateTime);
    Mono<TransactionEntity> getByIdWithDetails(UUID transactionId);
    Mono<TransactionEntity> save(TransactionEntity transactionEntity);
    Mono<TransactionEntity> processPayout(TransactionEntity transactionEntity, String merchantId);
}
