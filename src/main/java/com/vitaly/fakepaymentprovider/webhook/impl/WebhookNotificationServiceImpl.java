package com.vitaly.fakepaymentprovider.webhook.impl;

import com.vitaly.fakepaymentprovider.entity.TransactionEntity;
import com.vitaly.fakepaymentprovider.entity.WebhookEntity;
import com.vitaly.fakepaymentprovider.entity.util.Status;
import com.vitaly.fakepaymentprovider.exceptionhandling.InvalidWebhookDataException;
import com.vitaly.fakepaymentprovider.repository.WebhookRepository;
import com.vitaly.fakepaymentprovider.webhook.WebhookNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

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
                        .transactionAttempt(0L)
                        .createdBy("SYSTEM")
                        .updatedBy("SYSTEM")
                        .status(Status.IN_PROGRESS)
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
                .body(Mono.just(webhookEntity.getBodyRequest()), TransactionEntity.class)
                .retrieve()
                .bodyToMono(String.class)
                .map(responseBody -> webhookEntity.toBuilder()
                        .bodyResponse(responseBody)
                        .statusResponse(HttpStatus.OK.toString())
                        .build())
                .flatMap(webhookRepository::save)
                .onErrorResume(error -> {
                    webhookEntity.setStatusResponse(HttpStatus.INTERNAL_SERVER_ERROR.toString());
                    webhookEntity.setMessage(error.getMessage());
                    return webhookRepository.save(webhookEntity);
                });
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
