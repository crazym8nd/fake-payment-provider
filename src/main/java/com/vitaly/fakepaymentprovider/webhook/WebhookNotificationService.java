package com.vitaly.fakepaymentprovider.webhook;

import com.vitaly.fakepaymentprovider.entity.WebhookEntity;
import reactor.core.publisher.Mono;

public interface WebhookNotificationService {
    Mono<WebhookEntity> saveWebhook(WebhookEntity webhookEntity);
    Mono<WebhookEntity> updateWebhook(WebhookEntity webhookEntity);
    Mono<WebhookEntity> sendWebhook(WebhookEntity webhookEntity);
}
