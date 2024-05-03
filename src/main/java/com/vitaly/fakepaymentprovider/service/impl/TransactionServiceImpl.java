package com.vitaly.fakepaymentprovider.service.impl;

import com.vitaly.fakepaymentprovider.entity.TransactionEntity;
import com.vitaly.fakepaymentprovider.entity.util.Status;
import com.vitaly.fakepaymentprovider.repository.TransactionRepository;
import com.vitaly.fakepaymentprovider.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    @Override
    public Flux<TransactionEntity> getAll() {
        return transactionRepository.findAll();
    }

    @Override
    public Mono<TransactionEntity> getById(String transactionId) {
        return transactionRepository.findById(transactionId);
    }

    @Override
    public Mono<TransactionEntity> update(TransactionEntity transactionEntity) {
        return transactionRepository.save(transactionEntity.toBuilder()
                .updatedAt(LocalDateTime.now())
                .build());
    }

    @Override
    public Mono<TransactionEntity> save(TransactionEntity transactionEntity) {
        return transactionRepository.save(
                transactionEntity.toBuilder()
                        .paymentMethod(transactionEntity.getPaymentMethod())
                        .amount(transactionEntity.getAmount())
                        .currency(transactionEntity.getCurrency())
                        .language(transactionEntity.getLanguage())
                        .notificationUrl(transactionEntity.getNotificationUrl())
                        .accountId(transactionEntity.getAccountId())
                        .cardId(transactionEntity.getCardId())
                        .createdAt(transactionEntity.getCreatedAt())
                        .updatedAt(transactionEntity.getUpdatedAt())
                        .createdBy(transactionEntity.getCreatedBy())
                        .updatedBy(transactionEntity.getUpdatedBy())
                        .status(Status.ACTIVE)
                        .build()
        );
    }

    @Override
    public Mono<TransactionEntity> deleteById(String transactionId) {
        return transactionRepository.findById(transactionId)
                .flatMap(trans -> transactionRepository.deleteById(trans.getId()).thenReturn(trans));
    }
}
