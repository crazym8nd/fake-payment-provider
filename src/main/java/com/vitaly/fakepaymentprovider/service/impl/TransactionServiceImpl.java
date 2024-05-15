package com.vitaly.fakepaymentprovider.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vitaly.fakepaymentprovider.dto.webhook.WebhookDto;
import com.vitaly.fakepaymentprovider.entity.*;
import com.vitaly.fakepaymentprovider.entity.util.Status;
import com.vitaly.fakepaymentprovider.entity.util.TransactionType;
import com.vitaly.fakepaymentprovider.exceptionhandling.RequestPayoutTransactionInvalidAmountException;
import com.vitaly.fakepaymentprovider.exceptionhandling.RequestTopUpTransactionInvalidPaymentMethodException;
import com.vitaly.fakepaymentprovider.mapper.CardMapper;
import com.vitaly.fakepaymentprovider.mapper.CustomerMapper;
import com.vitaly.fakepaymentprovider.repository.TransactionRepository;
import com.vitaly.fakepaymentprovider.service.AccountService;
import com.vitaly.fakepaymentprovider.service.CardService;
import com.vitaly.fakepaymentprovider.service.CustomerService;
import com.vitaly.fakepaymentprovider.service.TransactionService;
import com.vitaly.fakepaymentprovider.webhook.WebhookNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final CardMapper cardMapper;
    private final CustomerMapper customerMapper;
    private final ObjectMapper objectMapper;
    private final TransactionRepository transactionRepository;
    private final CardService cardService;
    private final AccountService accountService;
    private final CustomerService customerService;
    private final WebhookNotificationService webhookNotificationService;



    //transactions topup

    @Override
    public Flux<TransactionEntity> getAll() {
        return transactionRepository.findAll();
    }

    @Override
    public Flux<TransactionEntity> getAllTransactionsByTypeAndPeriod(TransactionType type, LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findTopUpTransactions( type, startDate, endDate)
                .flatMap(transactionEntity ->
                        Mono.zip(
                                cardService.getById(transactionEntity.getCardNumber()),
                                customerService.getById(transactionEntity.getCardNumber()),
                                Mono.just(transactionEntity)
                        ).map(tuple -> {
                            CardEntity cardEntity = tuple.getT1();
                            CustomerEntity customerEntity = tuple.getT2();
                            TransactionEntity transaction = tuple.getT3();

                            transaction.setCardData(cardEntity);
                            transaction.setCustomer(customerEntity);
                            return transaction;
                        })
                );
    }

    @Override
    public Flux<TransactionEntity> getAllTransactionsByTypeAndDay(TransactionType type, LocalDate date) {
        return transactionRepository.findTransactionsByTypeAndDay( type, date)
                .flatMap(transactionEntity ->
                        Mono.zip(
                                cardService.getById(transactionEntity.getCardNumber()),
                                customerService.getById(transactionEntity.getCardNumber()),
                                Mono.just(transactionEntity)
                        ).map(tuple -> {
                            CardEntity cardEntity = tuple.getT1();
                            CustomerEntity customerEntity = tuple.getT2();
                            TransactionEntity transaction = tuple.getT3();

                            transaction.setCardData(cardEntity);
                            transaction.setCustomer(customerEntity);
                            return transaction;
                        })
                );
    }

    @Override
    public Mono<TransactionEntity> getById(UUID transactionId) {
        return transactionRepository.findById(transactionId);
    }

    @Override
    public Mono<TransactionEntity> getByIdWithDetails(UUID transactionId) {
        return transactionRepository.findById(transactionId)
                .flatMap(transactionEntity ->
                        Mono.zip(
                                Mono.just(transactionEntity),
                                customerService.getById(transactionEntity.getCardNumber())
                        ).map(tuple -> {
                            TransactionEntity transaction = tuple.getT1();
                            CustomerEntity customerEntity = tuple.getT2();
                            transaction.setCardData(CardEntity.builder()
                                    .cardNumber(transaction.getCardNumber())
                                    .build());
                            transaction.setCustomer(customerEntity);
                            return transaction;
                        })
                );
    }

    @Override
    public Mono<TransactionEntity> update(TransactionEntity transactionEntity) {
        return transactionRepository.save(transactionEntity.toBuilder()
                .updatedAt(LocalDateTime.now())
                .build());
    }

    @Override
    public Mono<TransactionEntity> save(TransactionEntity transactionEntity) {
        return transactionRepository.save(transactionEntity);
    }


    @Transactional
    @Override
    public Mono<TransactionEntity> processTopupTransaction(TransactionEntity transactionEntity, String merchantId) {
        if (transactionEntity.getTransactionId() == null) {
            return Mono.error(new IllegalArgumentException("Transaction ID cannot be null"));
        }

        transactionEntity.setCardNumber(transactionEntity.getCardData().getCardNumber());
        CustomerEntity customerEntity = CustomerEntity.builder()
                .firstName(transactionEntity.getCustomer().getFirstName())
                .lastName(transactionEntity.getCustomer().getLastName())
                .country(transactionEntity.getCustomer().getCountry())
                .cardNumber(transactionEntity.getCardNumber())
                .build();

        log.warn("Saving transaction {}", transactionEntity);
        log.warn("For merchant {}", merchantId);

        if (!transactionEntity.getPaymentMethod().equals("CARD")) {
            return Mono.error(new RequestTopUpTransactionInvalidPaymentMethodException("Invalid payment method: " + transactionEntity.getPaymentMethod()));
        } else {
            Mono<CardEntity> saveCardData = cardService.saveCardInTransaction(transactionEntity.getCardData());
            Mono<CustomerEntity> saveCustomerData = customerService.saveCustomerInTransaction(customerEntity);
            Mono<AccountEntity> saveAccountData = saveAccountData(transactionEntity, merchantId);

            return Mono.zip(saveCardData, saveCustomerData, saveAccountData)
                    .flatMap(tuple -> {
                        CardEntity cardEntity = tuple.getT1();
                        CustomerEntity savedCustomer = tuple.getT2();
                        transactionEntity.setCardNumber(cardEntity.getCardNumber());
                        transactionEntity.setCustomer(savedCustomer);

                        return saveAccountData
                                .flatMap(account -> Mono.just(account.getId()))
                                .flatMap(accountId -> {
                                    TransactionEntity transactionToSave = transactionEntity.toBuilder()
                                            .transactionType(TransactionType.TOPUP)
                                            .paymentMethod(transactionEntity.getPaymentMethod())
                                            .amount(transactionEntity.getAmount())
                                            .currency(transactionEntity.getCurrency())
                                            .language(transactionEntity.getLanguage())
                                            .notificationUrl(transactionEntity.getNotificationUrl())
                                            .cardNumber(cardEntity.getCardNumber())
                                            .accountId(accountId)
                                            .createdBy("SYSTEM")
                                            .updatedBy("SYSTEM")
                                            .build();
                                    String dtoJson = null;

                                    try {
                                        dtoJson = objectMapper.writeValueAsString(WebhookDto.builder()
                                                .transactionId(transactionToSave.getTransactionId())
                                                .paymentMethod(transactionToSave.getPaymentMethod())
                                                .amount(transactionToSave.getAmount())
                                                .currency(transactionToSave.getCurrency())
                                                .type(transactionToSave.getTransactionType().toString())
                                                .language(transactionToSave.getLanguage())
                                                .cardData(cardMapper.mapToWebhookCardDataDto(transactionToSave.getCardData()))
                                                .customer(customerMapper.mapToWebhookCustomerDto(transactionToSave.getCustomer()))
                                                .createdAt(transactionToSave.getCreatedAt())
                                                .updatedAt(transactionToSave.getUpdatedAt())
                                                .status(transactionToSave.getStatus())
                                                .message("OK")
                                                .build());
                                    } catch (JsonProcessingException e) {
                                        return Mono.error(new RuntimeException(e));
                                    }

                                    return webhookNotificationService.saveWebhook(WebhookEntity.builder()
                                                    .transactionId(transactionToSave.getTransactionId())
                                                    .transactionAttempt(0L)
                                                    .urlRequest(transactionToSave.getNotificationUrl())
                                                    .bodyRequest(dtoJson)
                                                    .createdAt(transactionToSave.getCreatedAt())
                                                    .createdBy(transactionToSave.getCreatedBy())
                                                    .status(transactionToSave.getStatus())
                                                    .build())
                                            .flatMap(webhookNotificationService::sendWebhook)
                                            .flatMap(webhookResponse -> transactionRepository.save(transactionToSave))
                                            .flatMap(savedTransaction -> transactionRepository.findById(savedTransaction.getTransactionId())
                                                    .flatMap(existingTransaction -> {
                                                        existingTransaction.setStatus(Status.SUCCESS);
                                                        return transactionRepository.save(existingTransaction);
                                                    })
                                                    .flatMap(updatedTransaction -> webhookNotificationService.saveWebhook(WebhookEntity.builder()
                                                                    .transactionId(updatedTransaction.getTransactionId())
                                                                    .transactionAttempt(0L)
                                                                    .urlRequest(updatedTransaction.getNotificationUrl())
                                                                    .bodyRequest("SOME TEXT WHEN SUCCESS")
                                                                    .createdBy("SYSTEM")
                                                                    .updatedBy("SYSTEM")
                                                                    .status(updatedTransaction.getStatus())
                                                                    .message("SOME MESSAGE")
                                                                    .build())
                                                            .flatMap(savedWebhook -> webhookNotificationService.sendWebhook(savedWebhook)
                                                                    .thenReturn(updatedTransaction))));
                                });
                    })
                    .doOnSuccess(savedTransaction -> log.warn("Transaction saved successfully: {}", savedTransaction))
                    .doOnError(error -> log.warn("Error saving transaction: {}", error.getMessage()));
        }
    }


    private Mono<TransactionEntity> asssd(TransactionEntity transactionEntity, String merchantId) {


        if (transactionEntity.getTransactionId() == null) {
            return Mono.error(new IllegalArgumentException("Transaction ID cannot be null"));
        }

        transactionEntity.setCardNumber(transactionEntity.getCardData().getCardNumber());
        CustomerEntity customerEntity = CustomerEntity.builder()
                .firstName(transactionEntity.getCustomer().getFirstName())
                .lastName(transactionEntity.getCustomer().getLastName())
                .country(transactionEntity.getCustomer().getCountry())
                .cardNumber(transactionEntity.getCardNumber())
                .build();

        log.warn("Saving transaction {}", transactionEntity);
        log.warn("For merchant {}", merchantId);

        if (!transactionEntity.getPaymentMethod().equals("CARD")) {
            return Mono.error(new RequestTopUpTransactionInvalidPaymentMethodException("Invalid payment method: " + transactionEntity.getPaymentMethod()));
        } else {
            Mono<CardEntity> saveCardData = cardService.saveCardInTransaction(transactionEntity.getCardData());
            Mono<CustomerEntity> saveCustomerData = customerService.saveCustomerInTransaction(customerEntity);
            Mono<AccountEntity> saveAccountData = saveAccountData(transactionEntity, merchantId);

            return Mono.zip(saveCardData, saveCustomerData, saveAccountData)
                    .flatMap(tuple -> {
                        CardEntity cardEntity = tuple.getT1();
                        CustomerEntity savedCustomer = tuple.getT2();
                        transactionEntity.setCardNumber(cardEntity.getCardNumber());
                        transactionEntity.setCustomer(savedCustomer);

                        return saveAccountData
                                .flatMap(account -> Mono.just(account.getId()))
                                .flatMap(accountId -> {
                                    TransactionEntity transactionToSave = transactionEntity.toBuilder()
                                            .transactionType(TransactionType.TOPUP)
                                            .paymentMethod(transactionEntity.getPaymentMethod())
                                            .amount(transactionEntity.getAmount())
                                            .currency(transactionEntity.getCurrency())
                                            .language(transactionEntity.getLanguage())
                                            .notificationUrl(transactionEntity.getNotificationUrl())
                                            .cardNumber(cardEntity.getCardNumber())
                                            .accountId(accountId)
                                            .createdBy("SYSTEM")
                                            .updatedBy("SYSTEM")
                                            .status(Status.SUCCESS)
                                            .build();

                                    return transactionRepository.save(transactionToSave)
                                            .flatMap(savedTransaction -> webhookNotificationService.saveWebhook(WebhookEntity.builder()
                                                            .transactionId(savedTransaction.getTransactionId())
                                                            .transactionAttempt(0L)
                                                            .urlRequest(savedTransaction.getNotificationUrl())
                                                            .bodyRequest("SOME TEXT")
                                                            .createdBy("SYSTEM")
                                                            .updatedBy("SYSTEM")
                                                            .status(savedTransaction.getStatus())
                                                            .message("SOME MESSAGE")
                                                            .build())
                                                    .flatMap(savedWebhook -> webhookNotificationService.sendWebhook(savedWebhook)
                                                            .thenReturn(savedTransaction)));
                                });
                    })
                    .doOnSuccess(savedTransaction -> log.warn("Transaction saved successfully: {}", savedTransaction))
                    .doOnError(error -> log.warn("Error saving transaction: {}", error.getMessage()));
        }
    }
    @Override
    public Mono<TransactionEntity> deleteById(UUID transactionId) {
        return transactionRepository.findById(transactionId)
                .flatMap(transaction -> transactionRepository.deleteById(transactionId)
                        .thenReturn(transaction));
    }

    private Mono<AccountEntity> saveAccountData(TransactionEntity transactionEntity, String merchantId) {
        return accountService.saveAccountInTransaction(
                AccountEntity.builder()
                        .merchantId(merchantId)
                        .currency(transactionEntity.getCurrency())
                        .amount(transactionEntity.getAmount())
                        .build());
    }


    //transactions payout
    @Override
    @Transactional
    public Mono<TransactionEntity> processPayoutTransaction(TransactionEntity transactionEntity, String merchantId) {
        transactionEntity.setTransactionId(UUID.randomUUID());
        transactionEntity.setCardNumber(transactionEntity.getCardData().getCardNumber());
        CustomerEntity customerEntity = CustomerEntity.builder()
                .firstName(transactionEntity.getCustomer().getFirstName())
                .lastName(transactionEntity.getCustomer().getLastName())
                .country(transactionEntity.getCustomer().getCountry())
                .cardNumber(transactionEntity.getCardNumber())
                .build();
        log.warn("Payout transaction: {}", transactionEntity);
        log.warn("For merchant: {}", merchantId);
        if (!transactionEntity.getPaymentMethod().equals("CARD")) {
            return Mono.error(new RequestTopUpTransactionInvalidPaymentMethodException("Invalid payment method: " + transactionEntity.getPaymentMethod()));
        } else {
            Mono<CardEntity> saveCardData = cardService.saveCardInTransaction(transactionEntity.getCardData());
            Mono<AccountEntity> accountMono = accountService.getByMerchantIdAndCurrency(merchantId, transactionEntity.getCurrency());
            Mono<CustomerEntity> saveCustomerData = customerService.saveCustomerInTransaction(customerEntity);

            return Mono.zip(accountMono, saveCustomerData)
                    .flatMap(tuple -> {
                        AccountEntity accountEntity = tuple.getT1();

                        BigDecimal accountAmount = accountEntity.getAmount();
                        if (accountAmount != null && accountAmount.compareTo(transactionEntity.getAmount()) >= 0) {
                            BigDecimal newAccountAmount = accountAmount.subtract(transactionEntity.getAmount());
                            return accountService.update(
                                            accountEntity.toBuilder()
                                                    .amount(newAccountAmount)
                                                    .updatedAt(LocalDateTime.now())
                                                    .build())
                                    .flatMap(updatedAccount -> transactionRepository.save(transactionEntity.toBuilder()
                                            .transactionType(TransactionType.PAYOUT)
                                            .paymentMethod(transactionEntity.getPaymentMethod())
                                            .amount(transactionEntity.getAmount())
                                            .currency(transactionEntity.getCurrency())
                                            .language(transactionEntity.getLanguage())
                                            .notificationUrl(transactionEntity.getNotificationUrl())
                                            .cardNumber(transactionEntity.getCardData().getCardNumber())
                                            .accountId(updatedAccount.getId())
                                            .customer(customerEntity)
                                            .createdBy("SYSTEM")
                                            .status(Status.SUCCESS)
                                            .build()));
                        } else {
                            return Mono.error(new RequestPayoutTransactionInvalidAmountException("PAYOUT_MIN_AMOUNT"));
                        }
                    });
        }
    }
}
