package com.vitaly.fakepaymentprovider.webhook.impl;

import com.vitaly.fakepaymentprovider.entity.WebhookEntity;
import com.vitaly.fakepaymentprovider.exceptionhandling.InvalidWebhookDataException;
import com.vitaly.fakepaymentprovider.repository.WebhookRepository;
import com.vitaly.fakepaymentprovider.webhook.WebhookNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookNotificationServiceImpl implements WebhookNotificationService {
   private final WebhookRepository webhookRepository;
   private final WebClient webClient = WebClient.builder().build();

    @Override
    public Mono<WebhookEntity> saveWebhook(WebhookEntity webhookEntity) {
        return  validateWebhook(webhookEntity)
                .flatMap(validatedWebhookEntity ->webhookRepository.save(webhookEntity.toBuilder()
                        .createdAt(LocalDateTime.now())
                        .createdBy("SYSTEM")
                .build()));
    }

    @Override
    public Mono<WebhookEntity> updateWebhook(WebhookEntity webhookEntity) {
        return webhookRepository.save(webhookEntity.toBuilder()
                        .updatedAt(LocalDateTime.now())
                .build());
    }

    @Override
    public Mono<WebhookEntity> sendWebhook(WebhookEntity webhookEntity) {

        return webClient.post()
                .uri(webhookEntity.getUrlRequest())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(webhookEntity.getBodyRequest()), String.class)
                .retrieve()
                .toEntity(String.class)
                .map(response -> webhookEntity.toBuilder()
                        .message("OK")
                        .bodyResponse(response.getBody())
                        .statusResponse(response.getStatusCode().toString())
                        .build())
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .doBeforeRetry(retrySignal -> {
                            webhookEntity.setTransactionAttempt(retrySignal.totalRetries());
                        }))
                .flatMap(webhookRepository::save)
                .onErrorResume(error -> {
                    webhookEntity.setMessage(error.getMessage());
                    return webhookRepository.save(webhookEntity);
                });
    }

    @Override
    public Mono<WebhookEntity> getByTransactionId(UUID transactionId) {
        return webhookRepository.findByTransactionId(transactionId);
    }

    private Mono<WebhookEntity> validateWebhook(WebhookEntity webhookEntity) {
        log.warn("Validating webhook {}", webhookEntity);
        if (webhookEntity.getTransactionId()== null) {
            return Mono.error(new InvalidWebhookDataException("Webhook ID cannot be null"));
        }

        if (webhookEntity.getUrlRequest() == null) {
            return Mono.error(new InvalidWebhookDataException("URL request cannot be null"));
        }

        if (webhookEntity.getBodyRequest() == null) {
            return Mono.error(new InvalidWebhookDataException("Body request cannot be null"));
        }
        return Mono.just(webhookEntity);
    }
}
