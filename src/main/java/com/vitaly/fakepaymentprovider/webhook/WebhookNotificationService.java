package com.vitaly.fakepaymentprovider.webhook;

import com.vitaly.fakepaymentprovider.entity.WebhookEntity;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface WebhookNotificationService {
    Mono<WebhookEntity> saveWebhook(WebhookEntity webhookEntity);
    Mono<WebhookEntity> updateWebhook(WebhookEntity webhookEntity);
    Mono<WebhookEntity> sendWebhook(WebhookEntity webhookEntity);
    Mono<WebhookEntity> getByTransactionId(UUID transactionId);
}
