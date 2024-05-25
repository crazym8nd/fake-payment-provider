package com.vitaly.fakepaymentprovider.repository;

import com.vitaly.fakepaymentprovider.entity.WebhookEntity;
import com.vitaly.fakepaymentprovider.entity.util.Status;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface WebhookRepository extends R2dbcRepository<WebhookEntity, Long> {
    Flux<WebhookEntity> findAllByStatus(Status status);
}
