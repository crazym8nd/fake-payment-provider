package com.vitaly.fakepaymentprovider.webhook;

import com.vitaly.fakepaymentprovider.entity.TransactionEntity;
import com.vitaly.fakepaymentprovider.entity.WebhookEntity;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface WebhookNotificationService {
    Mono<WebhookEntity> validateWebhook(WebhookEntity webhookEntity);
    Mono<TransactionEntity> createWebhook(Mono<TransactionEntity> transactionEntityMono);
    Mono<WebhookEntity> sendWebhook(WebhookEntity webhookEntity);
    Mono<WebhookEntity> updateWebhook(WebhookEntity webhookEntity);
    Mono<WebhookEntity> getByTransactionId(UUID transactionId);


//
//    .flatMap(savedTransaction -> {
//        try {
//            String dtoJson = objectMapper.writeValueAsString(WebhookDto.builder()
//                    .transactionId(transactionEntity.getTransactionId())
//                    .paymentMethod(transactionEntity.getPaymentMethod())
//                    .amount(transactionEntity.getAmount())
//                    .currency(transactionEntity.getCurrency())
//                    .type(transactionEntity.getTransactionType().toString())
//                    .language(transactionEntity.getLanguage())
//                    .cardData(cardMapper.mapToWebhookCardDataDto(transactionEntity.getCardData()))
//                    .customer(customerMapper.mapToWebhookCustomerDto(transactionEntity.getCustomer()))
//                    .createdAt(transactionEntity.getCreatedAt())
//                    .status(transactionEntity.getStatus())
//                    .message("OK")
//                    .build());
//
//            WebhookEntity webhookEntity = WebhookEntity.builder()
//                    .transactionId(savedTransaction.getTransactionId())
//                    .urlRequest(transactionEntity.getNotificationUrl())
//                    .transactionAttempt(0L)
//                    .bodyRequest(dtoJson)
//                    .createdAt(transactionEntity.getCreatedAt())
//                    .createdBy("SYSTEM")
//                    .status(transactionEntity.getStatus())
//                    .build();
//
//            log.warn("Saving webhook {}", webhookEntity);
//
//            return webhookNotificationService.saveWebhook(webhookEntity)
//                    .flatMap(webhookNotificationService::sendWebhook)
//                    .thenReturn(savedTransaction
//                    );
//
//        } catch (JsonProcessingException e) {
//            return Mono.error(new RuntimeException("Error processing JSON", e));
//        }
//    })
}
