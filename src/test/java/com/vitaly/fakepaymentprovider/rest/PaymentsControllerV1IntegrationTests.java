package com.vitaly.fakepaymentprovider.rest;

import com.vitaly.fakepaymentprovider.dto.requestdto.RequestTopupTransactionDto;
import com.vitaly.fakepaymentprovider.service.TransactionService;
import com.vitaly.fakepaymentprovider.util.TransactionDataUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import java.time.Duration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
class PaymentsControllerV1IntegrationTests {
    @Autowired
    private PasswordEncoder passwordEncoder;
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
    public void test_top_up_transaction_with_valid_parameters() {
        // Given
        RequestTopupTransactionDto dto = TransactionDataUtils.getDtoForRequestTest();
        String credentials = "PROSELYTE:b2eeea3e27834b7499dd7e01143a23dd";
        WebTestClient webTestClientWithTimeout = webTestClient.mutate()
                .responseTimeout(Duration.ofSeconds(60))
                .build();
        // When
        WebTestClient.ResponseSpec result = webTestClientWithTimeout.post().uri("/api/v1/payments/topups/")
                .header(HttpHeaders.AUTHORIZATION, "Basic UFJPU0VMWVRFOmIyZWVlYTNlMjc4MzRiNzQ5OWRkN2UwMTE0M2EyM2Rk")
                .body(Mono.just(dto),RequestTopupTransactionDto.class)
                .exchange();

        // Then
        result.expectStatus().isOk()
                .expectBody()
                .jsonPath("$.transaction_id").exists()
                .jsonPath("$.message").isEqualTo("OK")
                .jsonPath("$.status").isEqualTo("IN_PROGRESS");
    }
}