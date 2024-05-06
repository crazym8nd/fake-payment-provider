package com.vitaly.fakepaymentprovider.repository;

import com.vitaly.fakepaymentprovider.entity.CardEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface CardRepository extends R2dbcRepository<CardEntity, String> {
    @Modifying
    @Query("UPDATE cards SET status = 'DELETED' WHERE id = :id")
    Mono<Void> deleteById(String cardNumber);
}
