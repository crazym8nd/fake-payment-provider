package com.vitaly.fakepaymentprovider.repository;

import com.vitaly.fakepaymentprovider.entity.MerchantEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface MerchantRepository extends R2dbcRepository<MerchantEntity, String> {
    @Modifying
    @Query("UPDATE merchants SET status = 'DELETED' WHERE id = :id")
    Mono<Void> deleteById(String id);
}
