package com.vitaly.fakepaymentprovider.repository;

import com.vitaly.fakepaymentprovider.entity.AccountEntity;
import com.vitaly.fakepaymentprovider.entity.util.Currency;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface AccountRepository extends R2dbcRepository<AccountEntity, Long> {

    Mono<AccountEntity> findByMerchantIdAndCurrency(String merchantId, Currency currency);
    Flux<AccountEntity> findAllByMerchantId(String merchantId);
}
