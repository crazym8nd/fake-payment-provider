package com.vitaly.fakepaymentprovider.repository;

import com.vitaly.fakepaymentprovider.entity.WebhookEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface WebhookRepository extends R2dbcRepository<WebhookEntity, Long> {
    @Modifying
    @Query("UPDATE webhooks SET status = 'DELETED' WHERE id = :id")
    Mono<Void> deleteById(Long id);

    @Query("SELECT * FROM webhooks WHERE transaction_id = $1")
    Mono<WebhookEntity> findByTransactionId(UUID transactionId);
}
