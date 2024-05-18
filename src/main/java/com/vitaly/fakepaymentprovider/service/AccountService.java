package com.vitaly.fakepaymentprovider.service;

import com.vitaly.fakepaymentprovider.entity.AccountEntity;
import com.vitaly.fakepaymentprovider.entity.util.Currency;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountService extends GenericService<AccountEntity,Long>{
    Mono<AccountEntity> getByMerchantIdAndCurrency(String merchantId, Currency currency);
    Mono<AccountEntity> saveAccountInTransaction(AccountEntity accountEntity);
    Flux<AccountEntity> getAllAccountsForMerchant(String merchantId);
}
