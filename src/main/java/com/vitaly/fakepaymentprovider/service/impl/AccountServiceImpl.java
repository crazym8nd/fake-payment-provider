package com.vitaly.fakepaymentprovider.service.impl;

import com.vitaly.fakepaymentprovider.entity.AccountEntity;
import com.vitaly.fakepaymentprovider.entity.util.Currency;
import com.vitaly.fakepaymentprovider.entity.util.Status;
import com.vitaly.fakepaymentprovider.entity.util.TransactionType;
import com.vitaly.fakepaymentprovider.repository.AccountRepository;
import com.vitaly.fakepaymentprovider.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;

    @Override
    public Mono<AccountEntity> getById(Long accountId) {
        return accountRepository.findById(accountId);
    }


    @Override
    public Mono<AccountEntity> save(AccountEntity accountEntity) {
        return accountRepository.save(accountEntity);
    }



    @Override
    public Mono<AccountEntity> saveAccountForTransaction(AccountEntity accountEntity) {
        return getByMerchantIdAndCurrency(accountEntity.getMerchantId(), accountEntity.getCurrency())
                        .switchIfEmpty(accountRepository.save(
                                    accountEntity.toBuilder()
                                      .merchantId(accountEntity.getMerchantId())
                                      .currency(accountEntity.getCurrency())
                                       .amount(BigDecimal.ZERO)
                                        .createdBy("SYSTEM")
                                        .createdAt(LocalDateTime.now())
                                       .status(Status.ACTIVE)
                                    .build()));
    }

    @Override
    public Mono<AccountEntity> processTransaction(TransactionType type, Long accountId, BigDecimal amount) {
        return accountRepository.findById(accountId)
                .flatMap(existingAccount -> {
                    switch (type) {
                        case TOPUP:
                            existingAccount.setAmount(existingAccount.getAmount().add(amount));
                            break;
                        case PAYOUT:
                            existingAccount.setAmount(existingAccount.getAmount().subtract(amount));
                            break;
                        default:
                            return Mono.error(new IllegalArgumentException("Invalid transaction type: " + type));
                    }
                    existingAccount.setUpdatedAt(LocalDateTime.now());
                    existingAccount.setUpdatedBy("SYSTEM");

                    return accountRepository.save(existingAccount);
                });
    }


    @Override
    public Mono<AccountEntity> getByMerchantIdAndCurrency(String merchantId, Currency currency) {
        return accountRepository.findByMerchantIdAndCurrency(merchantId, currency);
    }

    @Override
    public Flux<AccountEntity> getAllAccountsForMerchant(String merchantId) {
        return accountRepository.findAllByMerchantId(merchantId);
    }
}
