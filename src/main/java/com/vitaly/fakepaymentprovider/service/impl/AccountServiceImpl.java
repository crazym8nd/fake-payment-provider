package com.vitaly.fakepaymentprovider.service.impl;

import com.vitaly.fakepaymentprovider.entity.AccountEntity;
import com.vitaly.fakepaymentprovider.entity.util.Currency;
import com.vitaly.fakepaymentprovider.entity.util.Status;
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
    public Flux<AccountEntity> getAll() {
        return accountRepository.findAll();
    }

    @Override
    public Mono<AccountEntity> getById(Long accountId) {
        return accountRepository.findById(accountId);
    }

    @Override
    public Mono<AccountEntity> update(AccountEntity accountEntity) {
        return accountRepository.save(accountEntity.toBuilder()
                .updatedAt(LocalDateTime.now())
                .build());
    }

    public Mono<AccountEntity> save(AccountEntity accountEntity) {
        return accountRepository.findByMerchantIdAndCurrency(accountEntity.getMerchantId(), accountEntity.getCurrency())
                .flatMap(existingAccount -> {
                    BigDecimal newAmount = existingAccount.getAmount().add(accountEntity.getAmount());
                    existingAccount.setAmount(newAmount);
                    existingAccount.setUpdatedAt(LocalDateTime.now());
                    return accountRepository.save(existingAccount);
                })
                .switchIfEmpty(Mono.defer(() -> accountRepository.save(
                        accountEntity.toBuilder()
                                .merchantId(accountEntity.getMerchantId())
                                .currency(accountEntity.getCurrency())
                                .amount(accountEntity.getAmount())
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .createdBy("SYSTEM")
                                .updatedBy("SYSTEM")
                                .status(Status.ACTIVE)
                                .build())));
    }

    @Override
    public Mono<AccountEntity> deleteById(Long accountId) {
        return accountRepository.findById(accountId)
                .flatMap(acc ->accountRepository.deleteById(acc.getId()).thenReturn(acc));
    }


    @Override
    public Mono<AccountEntity> getByMerchantIdAndCurrency(String merchantId, Currency currency) {
        return accountRepository.findByMerchantIdAndCurrency(merchantId, currency);
    }
}
