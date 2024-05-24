package com.vitaly.fakepaymentprovider.repository;

import com.vitaly.fakepaymentprovider.entity.WebhookEntity;
import com.vitaly.fakepaymentprovider.entity.util.Status;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface WebhookRepository extends R2dbcRepository<WebhookEntity, Long> {

    @Query("SELECT * FROM webhooks WHERE transaction_id = $1")
    Mono<WebhookEntity> findByTransactionId(UUID transactionId);

    Flux<WebhookEntity> findAllByStatus(Status status);
}
