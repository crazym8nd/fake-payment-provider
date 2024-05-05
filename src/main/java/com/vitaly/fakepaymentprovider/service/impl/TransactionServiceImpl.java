package com.vitaly.fakepaymentprovider.service.impl;

import com.vitaly.fakepaymentprovider.entity.TransactionEntity;
import com.vitaly.fakepaymentprovider.entity.util.Status;
import com.vitaly.fakepaymentprovider.exceptionhandling.TransactionRequestInvalidPaymentMethodException;
import com.vitaly.fakepaymentprovider.repository.TransactionRepository;
import com.vitaly.fakepaymentprovider.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

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
    public Mono<TransactionEntity> getById(UUID transactionId) {
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
        log.warn("Saving transaction{}", transactionEntity);

        if (!transactionEntity.getPaymentMethod().equals("CARD")) {
            return Mono.error(new TransactionRequestInvalidPaymentMethodException("Invalid payment method: " + transactionEntity.getPaymentMethod()));
        } else {
            return transactionRepository.save(
                            transactionEntity.toBuilder()
                                    .paymentMethod(transactionEntity.getPaymentMethod())
                                    .amount(transactionEntity.getAmount())
                                    .currency(transactionEntity.getCurrency())
                                    .language(transactionEntity.getLanguage())
                                    .notificationUrl(transactionEntity.getNotificationUrl())
                                    .cardData(transactionEntity.getCardData())
                                    .customer(transactionEntity.getCustomer())
                                    .createdAt(LocalDateTime.now())
                                    .updatedAt(LocalDateTime.now())
                                    .createdBy("SYSTEM")
                                    .updatedBy("SYSTEM")
                                    .status(Status.IN_PROGRESS)
                                    .build()
                    )
                    .doOnSuccess(savedTransaction -> log.warn("Transaction saved successfully: {}", savedTransaction))
                    .doOnError(error -> log.warn("Error saving transaction: {}", error.getMessage()));
        }
    }
    @Override
    public Mono<TransactionEntity> deleteById(UUID transactionId) {
        return transactionRepository.findById(transactionId)
                .flatMap(trans -> transactionRepository.deleteById(trans.getId()).thenReturn(trans));
    }
}
