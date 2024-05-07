package com.vitaly.fakepaymentprovider.service.impl;

import com.vitaly.fakepaymentprovider.entity.AccountEntity;
import com.vitaly.fakepaymentprovider.entity.CardEntity;
import com.vitaly.fakepaymentprovider.entity.TransactionEntity;
import com.vitaly.fakepaymentprovider.entity.util.Status;
import com.vitaly.fakepaymentprovider.exceptionhandling.TransactionRequestInvalidPaymentMethodException;
import com.vitaly.fakepaymentprovider.repository.TransactionRepository;
import com.vitaly.fakepaymentprovider.service.AccountService;
import com.vitaly.fakepaymentprovider.service.CardService;
import com.vitaly.fakepaymentprovider.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    private final CardService cardService;

    private final AccountService accountService;

    //transactions topup
    @Override
    public Flux<TransactionEntity> getAll() {
        return transactionRepository.findAll();
    }

    public Flux<TransactionEntity> getAllByPeriod(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return transactionRepository.findAllByCreatedAtBetween(startDateTime, endDateTime);
    }

    @Override
    public Mono<TransactionEntity> getById(UUID transactionId) {
        return transactionRepository.findById(transactionId);
    }

    @Override
    public Mono<TransactionEntity> getByIdWithDetails(UUID transactionId) {
       return transactionRepository.findById(transactionId)
               .map(transactionEntity -> {
                   transactionEntity.setCardData(CardEntity.builder()
                           .cardNumber(transactionEntity.getCardNumber()).build());
                   return transactionEntity;
               });
    }

    @Override
    public Mono<TransactionEntity> update(TransactionEntity transactionEntity) {
        return transactionRepository.save(transactionEntity.toBuilder()
                .updatedAt(LocalDateTime.now())
                .build());
    }



    @Override
    @Transactional
    public Mono<TransactionEntity> save(TransactionEntity transactionEntity) {
        log.warn("Saving transaction{}", transactionEntity);

        if (!transactionEntity.getPaymentMethod().equals("CARD")) {
            return Mono.error(new TransactionRequestInvalidPaymentMethodException("Invalid payment method: " + transactionEntity.getPaymentMethod()));
        } else {
            Mono<CardEntity> saveCardData = cardService.save(transactionEntity.getCardData());

            Mono<TransactionEntity> saveTransaction = transactionRepository.save(
                    transactionEntity.toBuilder()
                            .paymentMethod(transactionEntity.getPaymentMethod())
                            .amount(transactionEntity.getAmount())
                            .currency(transactionEntity.getCurrency())
                            .language(transactionEntity.getLanguage())
                            .notificationUrl(transactionEntity.getNotificationUrl())
                            .cardNumber(transactionEntity.getCardData().getCardNumber())
                            .customer(transactionEntity.getCustomer())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .createdBy("SYSTEM")
                            .updatedBy("SYSTEM")
                            .status(Status.IN_PROGRESS)
                            .build()
            );

            Mono<Tuple2<CardEntity, AccountEntity>> saveAccountAndCardZip = Mono.zip(saveCardData, saveAccountData(transactionEntity));
            return saveAccountAndCardZip.flatMap(tuple -> {
                        CardEntity cardEntity = tuple.getT1();
                        transactionEntity.setCardNumber(cardEntity.getCardNumber());
                        return saveTransaction.flatMap(savedTransaction -> {
                            savedTransaction.setCardNumber(cardEntity.getCardNumber());
                            return transactionRepository.save(savedTransaction);
                        });
                    })
                    .doOnSuccess(savedTransaction -> log.warn("Transaction saved successfully: {}", savedTransaction))
                    .doOnError(error -> log.warn("Error saving transaction: {}", error.getMessage()));
        }
    }
    @Override
    public Mono<TransactionEntity> deleteById(UUID transactionId) {
        return transactionRepository.findById(transactionId)
                .flatMap(transaction -> transactionRepository.deleteById(transaction.getId())
                        .thenReturn(transaction));
    }

    private Mono<AccountEntity> saveAccountData(TransactionEntity transactionEntity){
        return accountService.save(
                AccountEntity.builder()
                        .merchantId("PROSELYTE")
                        .currency(transactionEntity.getCurrency())
                        .amount(transactionEntity.getAmount())
                        .build());
    }


    //transactions payout
    @Override
    @Transactional
    public Mono<TransactionEntity> processPayout(TransactionEntity transactionEntity) {
        log.warn("Payout transaction: {}", transactionEntity);


      return null;
    }
}
