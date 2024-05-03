package com.vitaly.fakepaymentprovider.service.impl;

import com.vitaly.fakepaymentprovider.entity.AccountEntity;
import com.vitaly.fakepaymentprovider.entity.util.Status;
import com.vitaly.fakepaymentprovider.repository.AccountRepository;
import com.vitaly.fakepaymentprovider.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    @Override
    public Mono<AccountEntity> save(AccountEntity accountEntity) {
        return accountRepository.save(
                accountEntity.toBuilder()
                        .merchantId(accountEntity.getMerchantId())
                        .currency(accountEntity.getCurrency())
                        .amount(accountEntity.getAmount())
                        .createdAt(accountEntity.getCreatedAt())
                        .updatedAt(accountEntity.getCreatedAt())
                        .createdBy("SYSTEM")
                        .updatedBy("SYSTEM")
                        .status(Status.ACTIVE)
                        .build()
        );
    }

    @Override
    public Mono<AccountEntity> deleteById(Long accountId) {
        return accountRepository.findById(accountId)
                .flatMap(acc ->accountRepository.deleteById(acc.getId()).thenReturn(acc));
    }
}
