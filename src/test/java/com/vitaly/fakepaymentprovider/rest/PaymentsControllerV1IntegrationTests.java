package com.vitaly.fakepaymentprovider.rest;

import com.vitaly.fakepaymentprovider.dto.requestdto.RequestTopupTransactionDto;
import com.vitaly.fakepaymentprovider.entity.MerchantEntity;
import com.vitaly.fakepaymentprovider.repository.MerchantRepository;
import com.vitaly.fakepaymentprovider.service.TransactionService;
import com.vitaly.fakepaymentprovider.util.TransactionDataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
class PaymentsControllerV1IntegrationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:16-alpine"
    );

    //positive scenario

    @Test
    @DisplayName("Should return ok response for topup transaction")
    public void givenMerchantTransaction_whenProcessTransaction_thenOKResponse() {
        // Given
        RequestTopupTransactionDto dto = TransactionDataUtils.getDtoForRequestTest();
        String merchantId = "PROSELYTE";
        String secretKey = "b2eeea3e27834b7499dd7e01143a23dd";
        WebTestClient webTestClientWithTimeout = webTestClient.mutate()
                .responseTimeout(Duration.ofSeconds(60))
                .build();
        // When
        WebTestClient.ResponseSpec result = webTestClientWithTimeout.post().uri("/api/v1/payments/topups/")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((merchantId + ":" + secretKey).getBytes()))
                .body(Mono.just(dto),RequestTopupTransactionDto.class)
                .exchange();

        // Then
        result.expectStatus().isOk()
                .expectBody()
                .jsonPath("$.transaction_id").exists()
                .jsonPath("$.message").isEqualTo("OK")
                .jsonPath("$.status").isEqualTo("IN_PROGRESS");
    }

    @Test
    @DisplayName("Should return 400 response for wrong payment method")
    public void givenMerchantTransactionWithWrongPaymentMethod_whenProcessTransaction_then400ResponsePAYMENT_METHOD_NOT_ALLOWED() {
        // Given
        RequestTopupTransactionDto dto = TransactionDataUtils.getDtoForRequestTest();
        dto.setPaymentMethod("stick");
        String merchantId = "PROSELYTE";
        String secretKey = "b2eeea3e27834b7499dd7e01143a23dd";
        // When
        WebTestClient.ResponseSpec result = webTestClient.post().uri("/api/v1/payments/topups/")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((merchantId + ":" + secretKey).getBytes()))
                .body(Mono.just(dto),RequestTopupTransactionDto.class)
                .exchange();

        // Then
        result.expectStatus().is4xxClientError()
                .expectBody()
                .jsonPath("$.message").isEqualTo("PAYMENT_METHOD_NOT_ALLOWED")
                .jsonPath("$.status").isEqualTo("FAILED");
    }

    @Test
    @DisplayName("Should return transactions list with no params")
    public void givenMerchantID_whenGetTransactionsListWithoutParams_thenReturnListForToday() {
        // Given
        String merchantId = "TestMerchantID";
        String secretKey= "btjknbjefdskcjndkfgjekrgfnkrtjgn";
        // When
        WebTestClient.ResponseSpec result = webTestClient.get().uri("/api/v1/payments/transaction/list")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((merchantId + ":" + secretKey).getBytes()))
                .exchange();
// vozvrashaet vse transactii
        // Then
        result.expectStatus().isOk()
                .expectBody()
                .consumeWith(response -> {
                    String responseBody = Arrays.toString(response.getResponseBody());
                    System.out.println("Response Body: " + responseBody);
                });
    }

}