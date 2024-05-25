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
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

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
    public Mono<WebhookEntity> sendWebhook(WebhookEntity webhookEntity) {
        final AtomicLong retryCount = new AtomicLong(0L);
        return webClient.post()
                .uri(webhookEntity.getUrlRequest())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(webhookEntity.getBodyRequest())
                .retrieve()
                .toEntity(String.class)
                .flatMap(response -> webhookRepository.save(webhookEntity.toBuilder()
                                .bodyResponse(response.getBody())
                                .statusResponse(response.getStatusCode().toString())
                                .updatedBy("NotificationService")
                                .updatedAt(LocalDateTime.now())
                                .transactionAttempt(retryCount.get())
                                .message("OK")
                                .status(Status.SUCCESS)
                                .build()))
                .onErrorResume(WebClientResponseException.class, e -> {
                    retryCount.incrementAndGet();
                    log.warn("Error receiving response, status: {}, retry count: {}", e.getStatusCode(), retryCount.get());
                   if(retryCount.get() >= 3) {
                       return webhookRepository.save(webhookEntity.toBuilder()
                               .bodyResponse(e.getMessage())
                               .statusResponse(String.valueOf(e.getStatusCode()))
                               .updatedBy("NotificationService")
                               .updatedAt(LocalDateTime.now())
                               .transactionAttempt(retryCount.get())
                               .message(e.getMessage())
                               .status(Status.FAILED)
                               .build());
                   } else {
                       return Mono.error(e);
                   }
                })
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .doBeforeRetry(retrySignal -> retryCount.incrementAndGet()));
    }



    @Override
    public Mono<Void> sendWebhooks(Flux<WebhookEntity> webhooks) {
        return webhooks.flatMapSequential(this::sendWebhook)
                .then();
    }

    @Override
    @Scheduled(cron = "0 * * * * *")
    public void jobForSendingWebhooksInProgress() {
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
