package com.vitaly.fakepaymentprovider.service;

import com.vitaly.fakepaymentprovider.entity.AccountEntity;
import com.vitaly.fakepaymentprovider.entity.util.Currency;
import com.vitaly.fakepaymentprovider.entity.util.TransactionType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface AccountService extends GenericService<AccountEntity,Long>{
    Mono<AccountEntity> getByMerchantIdAndCurrency(String merchantId, Currency currency);
    Mono<AccountEntity> saveAccountForTransaction(AccountEntity accountEntity);
    Mono<AccountEntity> processTransaction(TransactionType type,Long accountId, BigDecimal amount);
    Flux<AccountEntity> getAllAccountsForMerchant(String merchantId);
}
