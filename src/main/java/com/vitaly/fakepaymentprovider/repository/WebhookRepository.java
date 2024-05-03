package com.vitaly.fakepaymentprovider.repository;

import com.vitaly.fakepaymentprovider.entity.WebhookEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface WebhookRepository extends R2dbcRepository<WebhookEntity, Long> {
    @Modifying
    @Query("UPDATE webhooks SET status = 'DELETED' WHERE id = :id")
    Mono<Void> deleteById(Long id);
}
