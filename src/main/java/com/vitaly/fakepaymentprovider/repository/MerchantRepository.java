package com.vitaly.fakepaymentprovider.repository;

import com.vitaly.fakepaymentprovider.entity.MerchantEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface MerchantRepository extends R2dbcRepository<MerchantEntity, String> {
    @Query("SELECT * FROM merchants WHERE merchant_id = $1")
    Mono<MerchantEntity> findByMerchantId(String merchantId);

    @Modifying
    @Query("UPDATE merchants SET status = 'DELETED' WHERE merchant_id = :id")
    Mono<Void> deleteById(String id);
}
