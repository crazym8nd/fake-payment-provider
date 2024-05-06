package com.vitaly.fakepaymentprovider.rest;

import com.vitaly.fakepaymentprovider.dto.responsedto.ResponseTopupTransactionDto;
import com.vitaly.fakepaymentprovider.entity.TransactionEntity;
import com.vitaly.fakepaymentprovider.service.TransactionService;
import com.vitaly.fakepaymentprovider.util.TransactionDataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
class PaymentsControllerV1IntegrationTests {
    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TransactionService transactionService;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:16-alpine"
    );
    //positive scenario
    @Test
    @DisplayName("Should successfully get transaction details")
    public void givenTransactionId_whenGetTransactionDetails_thenSuccessResponse() throws InterruptedException {
        //given

        UUID transactionId = transactionService.save(TransactionDataUtils.transactionForDetailsTest())
                .map(TransactionEntity::getTransactionId)
                .block();
        //when
        WebTestClient.ResponseSpec result = webTestClient.get()
                .uri("/api/v1/payments/transaction/{transactionId}/details",
                        transactionId)
                .exchange();
        //then
        result.expectStatus().isOk()
                .expectBody(ResponseTopupTransactionDto.class)
                .consumeWith(response -> {
                    ResponseTopupTransactionDto responseTopupTransactionDtoDetails = response.getResponseBody();
                    assertNotNull(responseTopupTransactionDtoDetails);
                    assertEquals(responseTopupTransactionDtoDetails.getTransactionId(),transactionId);
                });
    }

}