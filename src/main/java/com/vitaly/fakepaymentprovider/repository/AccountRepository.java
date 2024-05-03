package com.vitaly.fakepaymentprovider.repository;

import com.vitaly.fakepaymentprovider.entity.AccountEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface AccountRepository extends R2dbcRepository<AccountEntity, Long> {
    @Modifying
    @Query("UPDATE accounts SET status = 'DELETED' WHERE id = :id")
    Mono<Void> deleteById(Long id);

}
