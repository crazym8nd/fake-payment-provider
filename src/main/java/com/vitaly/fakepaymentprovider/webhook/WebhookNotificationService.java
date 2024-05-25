package com.vitaly.fakepaymentprovider.webhook;

import com.vitaly.fakepaymentprovider.entity.TransactionEntity;
import com.vitaly.fakepaymentprovider.entity.WebhookEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface WebhookNotificationService {
    Mono<WebhookEntity> createWebhook(TransactionEntity transactionEntity);
    Mono<Void> sendWebhooks(Flux<WebhookEntity> webhooks);
    Mono<WebhookEntity> sendWebhook(WebhookEntity webhookEntity);
    Mono<WebhookEntity> updateWebhook(UUID transactionId);
    Mono<WebhookEntity> getByTransactionId(UUID transactionId);
    void jobForSendingWebhooks();
}
