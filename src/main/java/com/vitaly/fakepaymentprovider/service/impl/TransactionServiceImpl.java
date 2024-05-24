package com.vitaly.fakepaymentprovider.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vitaly.fakepaymentprovider.entity.AccountEntity;
import com.vitaly.fakepaymentprovider.entity.CardEntity;
import com.vitaly.fakepaymentprovider.entity.CustomerEntity;
import com.vitaly.fakepaymentprovider.entity.TransactionEntity;
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
    public Mono<TransactionEntity> validateTopupTransaction(TransactionEntity transactionEntity) {
        if (!transactionEntity.getPaymentMethod().equals("CARD")) {
            return Mono.error(new RequestTopUpTransactionInvalidPaymentMethodException(
                    "Invalid payment method: " + transactionEntity.getPaymentMethod()));
        } else {
            return Mono.just(transactionEntity.toBuilder()
                    .transactionId(UUID.randomUUID())
                    .status(Status.IN_PROGRESS)
                    .createdAt(LocalDateTime.now())
                    .createdBy("SYSTEM")
                    .build());
        }
    }



    @Override
    public Flux<TransactionEntity> getAllTransactionsForMerchantByTypeAndPeriod(TransactionType type, LocalDateTime startDate, LocalDateTime endDate, String merchantId) {
        return accountService.getAllAccountsForMerchant(merchantId)
                .flatMap(account -> transactionRepository.findAllTransactionsForAccountByTypeAndPeriod(type, startDate, endDate, account.getId()))
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
    public Flux<TransactionEntity> getAllTopupTransactionsInProgress() {
        return transactionRepository.findAllByTransactionTypeTopupAndStatusInProgress();
    }

    @Override
    public Flux<TransactionEntity> getAllPayoutTransactionsInProgress() {
        return transactionRepository.findAllByTransactionTypePayoutAndStatusInProgress();
    }

    @Override
    public Flux<TransactionEntity> getAllTransactionsForMerchantByTypeAndDay(TransactionType type, LocalDate date, String merchantId) {
        return accountService.getAllAccountsForMerchant(merchantId)
                .flatMap(account -> transactionRepository.findAllTransactionsForAccountByTypeAndDay(type, date, account.getId()))
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
    public Mono<Void> processTopTransactionsInProgress(Flux<TransactionEntity> transactions) {
        return transactions.flatMapSequential(this::processTopupTransaction
        ).then();
    }

    @Override
    public Mono<TransactionEntity> update(TransactionEntity transactionEntity) {
        return transactionRepository.save(transactionEntity.toBuilder()
                .updatedAt(LocalDateTime.now())
                .build());
    }

    @Override
    public Mono<TransactionEntity> save(TransactionEntity transactionEntity) {
        log.warn("Saving transaction {}", transactionEntity);
        return transactionRepository.save(transactionEntity);
    }


    @Override
    public Mono<TransactionEntity> processTopupTransaction(TransactionEntity transactionEntity) {
            Mono<CardEntity> cardFromTransaction= cardService.getById(transactionEntity.getCardNumber());
            Mono<CustomerEntity> customerFromTransaction = customerService.getById(transactionEntity.getCardNumber());
            Mono<AccountEntity> accountFromTransaction = accountService.getById(transactionEntity.getAccountId());

            return Mono.zip(cardFromTransaction, customerFromTransaction, accountFromTransaction)
                    .flatMap(tuple -> {
                        transactionEntity.setCardData(tuple.getT1());
                        transactionEntity.setCustomer(tuple.getT2());
                        transactionEntity.setAccountId(tuple.getT3().getId());

                        return Mono.just(transactionEntity);
                    })
                    .flatMap(savedTransaction -> {
                        savedTransaction.setStatus(Status.SUCCESS);
                        return update(savedTransaction);
                    })
                    .flatMap(updatedTransaction -> webhookRepository.findByTransactionId(updatedTransaction.getTransactionId())
                    .doOnSuccess(updatedTransaction -> log.warn("Transaction saved successfully: {}", updatedTransaction))
                    .doOnError(error -> log.warn("Error saving transaction: {}", error.getMessage()));
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
    public Mono<TransactionEntity> validatePayoutTransaction(TransactionEntity transactionEntity,String merchantId) {
        return accountService.getByMerchantIdAndCurrency(merchantId, transactionEntity.getCurrency())
                .flatMap(account -> {
                    BigDecimal initialBalance = account.getAmount();
                    if (initialBalance.compareTo(transactionEntity.getAmount()) >= 0) {
                        BigDecimal changedBalance = initialBalance.subtract(transactionEntity.getAmount());
                        return accountService.update(
                                        account.toBuilder()
                                                .amount(changedBalance)
                                                .updatedAt(LocalDateTime.now())
                                                .updatedBy("SYSTEM")
                                                .build())
                                .flatMap(savedAccount -> Mono.just(transactionEntity.toBuilder()
                                        .transactionId(UUID.randomUUID())
                                        .status(Status.IN_PROGRESS)
                                        .createdAt(LocalDateTime.now())
                                        .createdBy("SYSTEM")
                                        .build()));
                    } else {
                        return Mono.error(new RequestPayoutTransactionInvalidAmountException("PAYOUT_MIN_AMOUNT"));
                    }
                })
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Account not found")));
    }

    @Override
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