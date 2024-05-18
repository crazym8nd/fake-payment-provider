package com.vitaly.fakepaymentprovider.rest;

import com.vitaly.fakepaymentprovider.dto.requestdto.RequestPayoutTransactionDto;
import com.vitaly.fakepaymentprovider.dto.requestdto.RequestTopupTransactionDto;
import com.vitaly.fakepaymentprovider.util.TransactionDataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
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
    // topup tansaction tests

    @Test
    @DisplayName("Should return ok response for topup transaction")
    public void givenMerchantTopupTransaction_whenProcessTransaction_thenOKResponse() {
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
                .jsonPath("$.status").exists();
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
                .jsonPath("$.transaction_list").exists();
    }

    @Test
    @DisplayName("Should return transactions list with params")
    public void givenMerchantID_whenGetTransactionsListWithParams_thenReturnListForPeriod() {
        // Given
        String merchantId = "TestMerchantID";
        String secretKey= "btjknbjefdskcjndkfgjekrgfnkrtjgn";
        String start_date="1714535453"; // 01-05-2024
        String end_date="1717127453"; // 31-05-2024
        // When
        WebTestClient.ResponseSpec result = webTestClient.get().uri("/api/v1/payments/transaction/list?start_date={start_date}&end_date={end_date}",start_date,end_date)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((merchantId + ":" + secretKey).getBytes()))
                .exchange();
        // vozvrashaet vse transactii
        // Then
        result.expectStatus().isOk()
                .expectBody()
                .jsonPath("$.transaction_list").exists();
    }

    @Test
    @DisplayName("Should return details for topup transaction")
    public void givenTransactionId_whenGetDetailsForTransaction_thenOKResponse() {
        // Given
        String merchantId = "TestMerchantID";
        String secretKey= "btjknbjefdskcjndkfgjekrgfnkrtjgn";
        String transactionId="b192e414-0d12-4129-9747-4fead5e8b6df";
        // When
        WebTestClient.ResponseSpec result = webTestClient.get().uri("/api/v1/payments/transaction/{transactionId}/details", transactionId)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((merchantId + ":" + secretKey).getBytes()))
                .exchange();

        // Then
        result.expectStatus().isOk()
                .expectBody()
                .jsonPath("$.transaction_id").isEqualTo(transactionId)
                .jsonPath("$.payment_method").isEqualTo("CARD")
                .jsonPath("$.amount").isEqualTo("1000")
                .jsonPath("$.currency").isEqualTo("USD")
                .jsonPath("$.created_at").exists()
                .jsonPath("$.updated_at").exists()
                .jsonPath("$..card_data.card_number").isEqualTo("4444555566668888")
                .jsonPath("$.language").isEqualTo("en")
                .jsonPath("$.notification_url").isEqualTo("https://test-webhook.free.beeceptor.com")
                .jsonPath("$.customer.first_name").isEqualTo("FirstNameFromSQLScriptTest")
                .jsonPath("$.customer.last_name").isEqualTo("LastNameFromSQLScriptTest")
                .jsonPath("$.customer.country").isEqualTo("CN")
                .jsonPath("$.message").isEqualTo("OK")
                .jsonPath("$.status").isEqualTo("IN_PROGRESS");
    }

    //payout tests

    @Test
    @DisplayName("Should return ok response for payout transaction")
    public void givenMerchantPayoutTransaction_whenProcessPayout_thenOKResponse() {
        // Given
        RequestPayoutTransactionDto dto = TransactionDataUtils.getPayoutDtoForRequestTest();
        String merchantId = "TestMerchantIDPayout";
        String secretKey = "btjknbjefdskcjndkfgjekrgfnkrtjgn";
        WebTestClient webTestClientWithTimeout = webTestClient.mutate()
                .responseTimeout(Duration.ofSeconds(60))
                .build();
        // When
        WebTestClient.ResponseSpec result = webTestClientWithTimeout.post().uri("/api/v1/payments/payout/")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((merchantId + ":" + secretKey).getBytes()))
                .body(Mono.just(dto),RequestPayoutTransactionDto.class)
                .exchange();

        // Then
        result.expectStatus().isOk()
                .expectBody()
                .jsonPath("$.transaction_id").exists()
                .jsonPath("$.message").isEqualTo("Payout is successfully completed")
                .jsonPath("$.status").isEqualTo("SUCCESS");
    }

    @Test
    @DisplayName("Should return 400 response for not enough amount")
    public void givenMerchantPayoutWithWrongPaymentMethod_whenProcessPayout_then400ResponsePAYOUT_MIN_AMOUNT() {
        // Given
        RequestPayoutTransactionDto dto = TransactionDataUtils.getPayoutDtoForRequestTest();
        dto.setAmount(BigDecimal.valueOf(999999L));
        String merchantId = "TestMerchantIDPayout";
        String secretKey = "btjknbjefdskcjndkfgjekrgfnkrtjgn";
        WebTestClient webTestClientWithTimeout = webTestClient.mutate()
                .responseTimeout(Duration.ofSeconds(60))
                .build();
        // When
        WebTestClient.ResponseSpec result = webTestClientWithTimeout.post().uri("/api/v1/payments/payout/")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((merchantId + ":" + secretKey).getBytes()))
                .body(Mono.just(dto),RequestPayoutTransactionDto.class)
                .exchange();

        // Then
        result.expectStatus().is4xxClientError()
                .expectBody()
                .jsonPath("$.message").isEqualTo("PAYOUT_MIN_AMOUNT")
                .jsonPath("$.error_code").isEqualTo("FAILED");
    }

    @Test
    @DisplayName("Should return payout list with no params")
    public void givenMerchantID_whenGetPayoutListWithoutParams_thenReturnPayoutListForToday() {
        // Given
        String merchantId = "TestMerchantIDPayout";
        String secretKey = "btjknbjefdskcjndkfgjekrgfnkrtjgn";
        // When
        WebTestClient.ResponseSpec result = webTestClient.get().uri("/api/v1/payments/payout/list")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((merchantId + ":" + secretKey).getBytes()))
                .exchange();
        // vozvrashaet vse transactii
        // Then
        result.expectStatus().isOk()
                .expectBody()
                .jsonPath("$.payout_list").exists();
    }

    @Test
    @DisplayName("Should return payout list with params")
    public void givenMerchantID_whenGetPayoutListWithParams_thenReturnPayoutListForPeriod() {
        // Given
        String merchantId = "TestMerchantIDPayout";
        String secretKey = "btjknbjefdskcjndkfgjekrgfnkrtjgn";
        String start_date="01-05-2024";
        String end_date="31-05-2024";
        // When
        WebTestClient.ResponseSpec result = webTestClient.get().uri("/api/v1/payments/payout/list?start_date={start_date}&end_date={end_date}",start_date,end_date)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((merchantId + ":" + secretKey).getBytes()))
                .exchange();
        // vozvrashaet vse transactii
        // Then
        result.expectStatus().isOk()
                .expectBody()
                .jsonPath("$.payout_list").exists();
    }

    @Test
    @DisplayName("Should return details for payout transaction")
    public void givenPayoutTransactionId_whenGetDetailsForPayoutTransaction_thenOKResponse() {
        // Given
        String merchantId = "TestMerchantIDPayout";
        String secretKey = "btjknbjefdskcjndkfgjekrgfnkrtjgn";
        String payoutId="54384d46-1212-44f8-933c-f4b9d8964c92";
        // When
        WebTestClient.ResponseSpec result = webTestClient.get().uri("/api/v1/payments/payout/{payoutId}/details", payoutId)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((merchantId + ":" + secretKey).getBytes()))
                .exchange();

        // Then
        result.expectStatus().isOk()
                .expectBody()
                .jsonPath("$.transaction_id").isEqualTo(payoutId)
                .jsonPath("$.payment_method").isEqualTo("CARD")
                .jsonPath("$.amount").isEqualTo("10")
                .jsonPath("$.currency").isEqualTo("USD")
                .jsonPath("$.created_at").exists()
                .jsonPath("$.updated_at").exists()
                .jsonPath("$..card_data.card_number").isEqualTo("1111***4444")
                .jsonPath("$.language").isEqualTo("en")
                .jsonPath("$.notification_url").isEqualTo("https://test-webhook.free.beeceptor.com")
                .jsonPath("$.customer.first_name").isEqualTo("FirstNamePayoutScriptTest")
                .jsonPath("$.customer.last_name").isEqualTo("LastNamePayoutScriptTest")
                .jsonPath("$.customer.country").isEqualTo("US");
    }

}
