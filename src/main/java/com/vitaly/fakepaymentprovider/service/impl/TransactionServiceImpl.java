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
import com.vitaly.fakepaymentprovider.repository.WebhookRepository;
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
    private final WebhookRepository webhookRepository;


    //transactions topup

    @Override
    public Flux<TransactionEntity> getAll() {
        return transactionRepository.findAll();
    }

    @Override
    public Flux<TransactionEntity> getAllTransactionsByTypeAndPeriod(TransactionType type, LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findTopUpTransactions(type, startDate, endDate)
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
        return transactionRepository.findTransactionsByTypeAndDay(type, date)
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

            log.warn("Saving topup transaction: {}", transactionEntity);
            return Mono.zip(saveCardData, saveCustomerData, saveAccountData)
                    .flatMap(tuple -> {
                        CardEntity cardEntity = tuple.getT1();
                        CustomerEntity savedCustomer = tuple.getT2();
                        AccountEntity accountEntity = tuple.getT3();

                        transactionEntity.setCardNumber(cardEntity.getCardNumber());
                        transactionEntity.setCustomer(savedCustomer);
                        transactionEntity.setAccountId(accountEntity.getId());
                        return transactionRepository.save(transactionEntity);
                    })
                    .flatMap(savedTransaction -> {
                        try {
                            String dtoJson = objectMapper.writeValueAsString(WebhookDto.builder()
                                    .transactionId(transactionEntity.getTransactionId())
                                    .paymentMethod(transactionEntity.getPaymentMethod())
                                    .amount(transactionEntity.getAmount())
                                    .currency(transactionEntity.getCurrency())
                                    .type(transactionEntity.getTransactionType().toString())
                                    .language(transactionEntity.getLanguage())
                                    .cardData(cardMapper.mapToWebhookCardDataDto(transactionEntity.getCardData()))
                                    .customer(customerMapper.mapToWebhookCustomerDto(transactionEntity.getCustomer()))
                                    .createdAt(transactionEntity.getCreatedAt())
                                    .status(transactionEntity.getStatus())
                                    .message("OK")
                                    .build());

                            WebhookEntity webhookEntity = WebhookEntity.builder()
                                    .transactionId(savedTransaction.getTransactionId())
                                    .transactionAttempt(0L)
                                    .urlRequest(transactionEntity.getNotificationUrl())
                                    .bodyRequest(dtoJson)
                                    .message("OK")
                                    .createdAt(transactionEntity.getCreatedAt())
                                    .createdBy("SYSTEM")
                                    .status(transactionEntity.getStatus())
                                    .build();

                            log.warn("Saving webhook {}", webhookEntity);

                            return webhookNotificationService.saveWebhook(webhookEntity)
                                    .flatMap(webhookNotificationService::sendWebhook)
                                    .thenReturn(savedTransaction
                                    );

                        } catch (JsonProcessingException e) {
                            return Mono.error(new RuntimeException("Error processing JSON", e));
                        }
                    })
                    .flatMap(savedTransaction -> {
                        savedTransaction.setStatus(Status.SUCCESS);
                        return update(savedTransaction);
                    })
                    .flatMap(updatedTransaction -> webhookRepository.findByTransactionId(updatedTransaction.getTransactionId())
                            .flatMap(webhookEntity -> {
                                try {
                                    String dtoJson = objectMapper.writeValueAsString(WebhookDto.builder()
                                            .transactionId(updatedTransaction.getTransactionId())
                                            .paymentMethod(updatedTransaction.getPaymentMethod())
                                            .amount(updatedTransaction.getAmount())
                                            .currency(updatedTransaction.getCurrency())
                                            .type(updatedTransaction.getTransactionType().toString())
                                            .language(updatedTransaction.getLanguage())
                                            .cardData(cardMapper.mapToWebhookCardDataDto(updatedTransaction.getCardData()))
                                            .customer(customerMapper.mapToWebhookCustomerDto(updatedTransaction.getCustomer()))
                                            .createdAt(updatedTransaction.getCreatedAt())
                                            .status(updatedTransaction.getStatus())
                                            .message("OK")
                                            .build());
                                    webhookEntity.setBodyRequest(dtoJson);
                                    webhookEntity.setStatus(updatedTransaction.getStatus());
                                    webhookEntity.setUpdatedAt(LocalDateTime.now());
                                    webhookEntity.setUpdatedBy("SYSTEM");
                                    log.warn("Saving webhook {}", webhookEntity);
                                    return webhookNotificationService.saveWebhook(webhookEntity)
                                            .flatMap(webhookNotificationService::sendWebhook)
                                            .thenReturn(updatedTransaction);
                                } catch (JsonProcessingException e) {
                                    return Mono.error(new RuntimeException("Error processing JSON", e));
                                }
                            }))
                    .doOnSuccess(updatedTransaction -> log.warn("Transaction saved successfully: {}", updatedTransaction))
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
        log.warn("Payout transaction: {}", transactionEntity);
        log.warn("For merchant: {}", merchantId);

        if (!transactionEntity.getPaymentMethod().equals("CARD")) {
            return Mono.error(new RequestTopUpTransactionInvalidPaymentMethodException("Invalid payment method: " + transactionEntity.getPaymentMethod()));
        } else {
            Mono<CardEntity> saveCardData = cardService.saveCardInTransaction(transactionEntity.getCardData());
            Mono<AccountEntity> accountMono = accountService.getByMerchantIdAndCurrency(merchantId, transactionEntity.getCurrency());
            Mono<CustomerEntity> saveCustomerData = customerService.saveCustomerInTransaction(customerEntity);

            log.warn("Saving payout transaction: {}", transactionEntity);
            return Mono.zip(saveCardData, saveCustomerData, accountMono)
                    .flatMap(tuple -> {
                        CardEntity cardEntity = tuple.getT1();
                        CustomerEntity savedCustomer = tuple.getT2();
                        AccountEntity accountEntity = tuple.getT3();

                        transactionEntity.setCardNumber(cardEntity.getCardNumber());
                        transactionEntity.setCustomer(savedCustomer);
                        transactionEntity.setAccountId(accountEntity.getId());


                        return save(transactionEntity.toBuilder()
                                .transactionType(TransactionType.PAYOUT)
                                .paymentMethod(transactionEntity.getPaymentMethod())
                                .amount(transactionEntity.getAmount())
                                .currency(transactionEntity.getCurrency())
                                .language(transactionEntity.getLanguage())
                                .notificationUrl(transactionEntity.getNotificationUrl())
                                .createdBy("SYSTEM")
                                .status(Status.IN_PROGRESS)
                                .updatedAt(LocalDateTime.now())
                                .build())
                                .flatMap(savedTransaction -> {
                                    BigDecimal accountAmount = accountEntity.getAmount();
                                    if (accountAmount != null && accountAmount.compareTo(transactionEntity.getAmount()) >= 0) {
                                        BigDecimal newAccountAmount = accountAmount.subtract(transactionEntity.getAmount());
                                        return accountService.update(
                                                        accountEntity.toBuilder()
                                                                .amount(newAccountAmount)
                                                                .updatedAt(LocalDateTime.now())
                                                                .build())
                                                .then(transactionRepository.save(transactionEntity.toBuilder()
                                                        .transactionType(TransactionType.PAYOUT)
                                                        .paymentMethod(transactionEntity.getPaymentMethod())
                                                        .amount(transactionEntity.getAmount())
                                                        .currency(transactionEntity.getCurrency())
                                                        .language(transactionEntity.getLanguage())
                                                        .notificationUrl(transactionEntity.getNotificationUrl())
                                                        .createdBy("SYSTEM")
                                                        .status(Status.SUCCESS)
                                                        .updatedAt(LocalDateTime.now())
                                                        .build()));
                                    } else {
                                        return Mono.error(new RequestPayoutTransactionInvalidAmountException("PAYOUT_MIN_AMOUNT"));
                                    }
                                });
                    });
        }
    }
}