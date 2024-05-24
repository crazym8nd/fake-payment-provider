package com.vitaly.fakepaymentprovider.webhook.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vitaly.fakepaymentprovider.dto.webhook.WebhookDto;
import com.vitaly.fakepaymentprovider.entity.TransactionEntity;
import com.vitaly.fakepaymentprovider.entity.WebhookEntity;
import com.vitaly.fakepaymentprovider.entity.util.Status;
import com.vitaly.fakepaymentprovider.mapper.CardMapper;
import com.vitaly.fakepaymentprovider.mapper.CustomerMapper;
import com.vitaly.fakepaymentprovider.repository.WebhookRepository;
import com.vitaly.fakepaymentprovider.webhook.WebhookNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
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
    private final ObjectMapper mapper;
    private final CardMapper cardMapper;
    private final CustomerMapper customerMapper;

    @Override
    public Mono<WebhookEntity> updateWebhook(UUID transactionId) {
        return webhookRepository.findByTransactionId(transactionId)
                .flatMap(webhookEntity -> webhookRepository.save(webhookEntity.toBuilder()
                        .updatedAt(LocalDateTime.now())
                        .updatedBy("SYSTEM")
                        .build()));
    }

    @Override
    public Mono<WebhookEntity> sendWebhook(WebhookEntity webhookEntity) {
        //TODO need to save error response
        WebClient.ResponseSpec response = webClient.post()
                .uri(webhookEntity.getUrlRequest())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(webhookEntity.getBodyRequest()), String.class)
                .retrieve();

        return response
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                    clientResponse.toEntity(String.class)
                            .map(responseString -> webhookEntity.toBuilder()
                                    .updatedBy("NotificationService")
                                    .updatedAt(LocalDateTime.now())
                                    .message("ERROR")
                                    .bodyResponse(responseString.getBody())
                                    .statusResponse(responseString.getStatusCode().toString())
                                    .build())
                            .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                                    .doBeforeRetry(retrySignal -> {
                                        log.warn("Error receiving response {}, status {}", webhookEntity.getBodyResponse(), webhookEntity.getStatusResponse());
                                        webhookRepository.save(webhookEntity.toBuilder()
                                                .updatedBy("NotificationService")
                                                .updatedAt(LocalDateTime.now())
                                                .transactionAttempt(retrySignal.totalRetries())
                                                .build()).subscribe();
                                    }))
                            .subscribe();
                    return null;
                })
                .toEntity(String.class)
                .map(responseEntity ->
                        webhookEntity.toBuilder()
                                .updatedBy("NotificationService")
                                .updatedAt(LocalDateTime.now())
                                .message("OK")
                                .bodyResponse(responseEntity.getBody())
                                .statusResponse(responseEntity.getStatusCode().toString())
                                .build())
                .onErrorResume(throwable -> Mono.just(webhookEntity.toBuilder()
                                .message(throwable.getMessage())
                                .build())
                        .flatMap(webhookRepository::save))
                .flatMap(webhookRepository::save);
    }

    @Override
    public Mono<Void> sendWebhooks(Flux<WebhookEntity> webhooks) {
        return webhooks.flatMapSequential(this::sendWebhook)
                .then();
    }

    @Override
    public Mono<WebhookEntity> getByTransactionId(UUID transactionId) {
        return webhookRepository.findByTransactionId(transactionId);
    }

    @Override
    @Scheduled(cron = "0 * * * * *")
    public void jobForSendingWebhooks() {
        sendWebhooks(webhookRepository.findAllByStatus(Status.IN_PROGRESS))
                .doOnSuccess(v -> log.warn("webhooks in progress sent"))
                .subscribe();
    }

    @Override
    public Mono<WebhookEntity> createWebhook(TransactionEntity transactionEntity) {
        return webhookRepository.save(WebhookEntity.builder()
                .transactionId(transactionEntity.getTransactionId())
                .transactionAttempt(0L)
                .urlRequest(transactionEntity.getNotificationUrl())
                .bodyRequest(convertToBody(transactionEntity))
                .createdAt(LocalDateTime.now())
                .createdBy("SYSTEM")
                .status(Status.IN_PROGRESS)
                .build())
                .doOnSuccess( webhook -> log.warn("Webhook created successfully: {}", webhook));
    }

    @SneakyThrows
    private String convertToBody(TransactionEntity transactionEntity){
        return mapper.writeValueAsString(WebhookDto.builder()
                .transactionId(transactionEntity.getTransactionId())
                .paymentMethod(transactionEntity.getPaymentMethod())
                .amount(transactionEntity.getAmount())
                .currency(transactionEntity.getCurrency())
                .type(transactionEntity.getTransactionType().toString())
                .language(transactionEntity.getLanguage())
                .cardData(cardMapper.mapToWebhookCardDataDto(transactionEntity.getCardData()))
                .customer(customerMapper.mapToWebhookCustomerDto(transactionEntity.getCustomer()))
                .createdAt(transactionEntity.getCreatedAt())
                .status(transactionEntity.getStatus())
                .message("OK")
                .build());
    }
}
