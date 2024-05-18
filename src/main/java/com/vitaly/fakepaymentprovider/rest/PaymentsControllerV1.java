package com.vitaly.fakepaymentprovider.rest;

import com.vitaly.fakepaymentprovider.dto.requestdto.RequestPayoutTransactionDto;
import com.vitaly.fakepaymentprovider.dto.requestdto.RequestTopupTransactionDto;
import com.vitaly.fakepaymentprovider.dto.responsedto.ResponsePayoutDetailsDto;
import com.vitaly.fakepaymentprovider.dto.responsedto.ResponsePayoutsListDto;
import com.vitaly.fakepaymentprovider.dto.responsedto.ResponseTransactionDetailsDto;
import com.vitaly.fakepaymentprovider.dto.responsedto.ResponseTransactionsListDto;
import com.vitaly.fakepaymentprovider.entity.TransactionEntity;
import com.vitaly.fakepaymentprovider.entity.util.Status;
import com.vitaly.fakepaymentprovider.entity.util.TransactionType;
import com.vitaly.fakepaymentprovider.mapper.TransactionMapper;
import com.vitaly.fakepaymentprovider.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/payments")
@Slf4j
public class PaymentsControllerV1 {
    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    //topups endpoints
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/topups/")
    public Mono<ResponseEntity<Map<String, String>>> testTopUpTransaction(@RequestBody RequestTopupTransactionDto requestTopupTransactionDto, Authentication authentication){
        TransactionEntity newTopupTransaction = transactionMapper.mapFromRequestTopupDto(requestTopupTransactionDto).toBuilder()
                .transactionId(UUID.randomUUID())
                .transactionType(TransactionType.TOPUP)
                .status(Status.IN_PROGRESS)
                .createdAt(LocalDateTime.now())
                .createdBy("SYSTEM")
                .build();

        return Mono.just(newTopupTransaction)
                .flatMap(topupTransaction -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("transaction_id", newTopupTransaction.getTransactionId().toString());
                    response.put("status", newTopupTransaction.getStatus().toString());
                    response.put("message", "OK");
                    return Mono.just(response);
                })
                .map(ResponseEntity::ok)
                .flatMap(initialResponse -> transactionService.processTopupTransaction(newTopupTransaction, authentication.getName())
                        .map(savedTransaction -> {
                            initialResponse.getBody().put("transaction_id", savedTransaction.getTransactionId().toString());
                            initialResponse.getBody().put("status", savedTransaction.getStatus().toString());
                            return initialResponse;
                        }));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/transaction/list")
    public Mono<ResponseEntity<ResponseTransactionsListDto>> getAllTransactionsList(
            @RequestParam(value = "start_date", required = false) Long startDateUnix,
            @RequestParam(value = "end_date", required = false) Long endDateUnix,
            Authentication authentication) {
        LocalDateTime startDate;
        LocalDateTime endDate;
        String merchantId = authentication.getName();
        if(startDateUnix!=null & startDateUnix!=null){
            startDate = Instant.ofEpochSecond(startDateUnix).atZone(ZoneOffset.UTC).toLocalDateTime();
            endDate = Instant.ofEpochSecond(endDateUnix).atZone(ZoneOffset.UTC).toLocalDateTime();
            return transactionService.getAllTransactionsForMerchantByTypeAndPeriod(TransactionType.TOPUP, startDate, endDate, merchantId)
                    .collectList()
                    .flatMap(list -> Mono.just(ResponseTransactionsListDto.builder()
                            .transactionList(list.stream()
                                    .map(transactionMapper::mapToResponseTransactionWithDetailsDto)
                                    .peek(dto -> dto.setMessage("OK"))
                                    .collect(Collectors.toList()))
                            .build()))
                    .map(ResponseEntity::ok);
        } else {
            LocalDate today = LocalDate.now(ZoneOffset.UTC);
            return transactionService.getAllTransactionsForMerchantByTypeAndDay(TransactionType.TOPUP, today, merchantId)
                    .collectList()
                    .flatMap(list -> Mono.just(ResponseTransactionsListDto.builder()
                            .transactionList(list.stream()
                                    .map(transactionMapper::mapToResponseTransactionWithDetailsDto)
                                    .peek(dto -> dto.setMessage("OK"))
                                    .collect(Collectors.toList()))
                            .build()))
                    .map(ResponseEntity::ok);
        }

    }
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/transaction/{transactionId}/details")
    public Mono<ResponseEntity<ResponseTransactionDetailsDto>> getTransactionDetails(@PathVariable UUID transactionId){
        return transactionService.getByIdWithDetails(transactionId)
                .map(transactionEntity -> {
                    ResponseTransactionDetailsDto dto = transactionMapper.mapToResponseTransactionWithDetailsDto(transactionEntity);
                    dto.setMessage("OK");
                return dto;})
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .doOnError(error -> log.warn("Error retrieving transaction details: {}", error.getMessage()));
    }

    //payout endpoints
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/payout/")
    public Mono<ResponseEntity<Map<String, String>>> createPayoutTransaction(@RequestBody RequestPayoutTransactionDto payoutDto, Authentication authentication) {
        TransactionEntity newPayoutTransaction = transactionMapper.mapFromRequestPayoutDto(payoutDto).toBuilder()
                .transactionId(UUID.randomUUID())
                .transactionType(TransactionType.PAYOUT)
                .status(Status.IN_PROGRESS)
                .createdAt(LocalDateTime.now())
                .createdBy("SYSTEM")
                .build();

        return Mono.just(newPayoutTransaction)
                .flatMap(payoutTransaction -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("transaction_id", payoutTransaction.getTransactionId().toString());
                    response.put("status", payoutTransaction.getStatus().toString());
                    response.put("message", "Payout is successfully completed");
                    return Mono.just(response);
                })
                .map(ResponseEntity::ok)
                .flatMap(initialResponse -> transactionService.processPayoutTransaction(newPayoutTransaction, authentication.getName())
                        .map(savedTransaction -> {
                            initialResponse.getBody().put("transaction_id", savedTransaction.getTransactionId().toString());
                            initialResponse.getBody().put("status", savedTransaction.getStatus().toString());
                            return initialResponse;
                        }));
    }



    @PreAuthorize("isAuthenticated()")
    @PostMapping("/testpayout/")
    public Mono<ResponseEntity<Map<String, String>>> teastPayoutTransaction(@RequestBody RequestPayoutTransactionDto payoutDto, Authentication authentication) {
        TransactionEntity newPayoutTransaction = transactionMapper.mapFromRequestPayoutDto(payoutDto).toBuilder()
                .transactionId(UUID.randomUUID())
                .status(Status.IN_PROGRESS)
                .build();
        return Mono.just(newPayoutTransaction)
                .flatMap(payoutTransaction -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("transaction_id", payoutTransaction.getTransactionId().toString());
                    response.put("status", payoutTransaction.getStatus().toString());
                    response.put("message", "Payout is successfully completed");
                    return Mono.just(response);
                })
                .map(ResponseEntity::ok)
                .flatMap(initialResponse -> transactionService.processPayoutTransaction(newPayoutTransaction, authentication.getName())
                        .map(savedTransaction -> {
                            initialResponse.getBody().put("transaction_id", savedTransaction.getTransactionId().toString());
                            initialResponse.getBody().put("status", savedTransaction.getStatus().toString());
                            return initialResponse;
                        }));
    }

    @PreAuthorize("isAuthenticated()")
   @GetMapping("/payout/list")
   public Mono<ResponseEntity<ResponsePayoutsListDto>> getAllPayoutsList(
           @RequestParam(value = "start_date", required = false)  String startDateStr,
           @RequestParam(value = "end_date", required = false)  String endDateStr,
           Authentication authentication) {
       LocalDateTime startDate;
       LocalDateTime endDate;
       String merchantId = authentication.getName();
       DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
       if(startDateStr!= null && endDateStr!= null) {
           startDate = LocalDate.parse(startDateStr, formatter).atStartOfDay(ZoneOffset.UTC).toLocalDateTime();
           endDate = LocalDate.parse(endDateStr, formatter).atTime(23, 59, 59).atZone(ZoneOffset.UTC).toLocalDateTime();
           return transactionService.getAllTransactionsForMerchantByTypeAndPeriod(TransactionType.PAYOUT, startDate, endDate, merchantId)
                   .collectList()
                   .flatMap(list -> Mono.just(ResponsePayoutsListDto.builder()
                           .payoutList(list.stream()
                                   .map(transactionMapper::mapToResponseTransactionWithDetailsDto)
                                   .peek(dto -> dto.setMessage("OK"))
                                   .collect(Collectors.toList()))
                           .build()))
                   .map(ResponseEntity::ok);

       } else {
           LocalDate today = LocalDate.now(ZoneOffset.UTC);
           return transactionService.getAllTransactionsForMerchantByTypeAndDay(TransactionType.PAYOUT, today, merchantId)
                   .collectList()
                   .flatMap(list -> Mono.just(ResponsePayoutsListDto.builder()
                           .payoutList(list.stream()
                                   .map(transactionMapper::mapToResponseTransactionWithDetailsDto)
                                   .peek(dto -> dto.setMessage("OK"))
                                   .collect(Collectors.toList()))
                           .build()))
                   .map(ResponseEntity::ok);
       }
   }


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/payout/{payoutId}/details")
    public Mono<ResponseEntity<ResponsePayoutDetailsDto>> getPayoutDetails(@PathVariable UUID payoutId){
        return transactionService.getByIdWithDetails(payoutId)
                .map(transactionEntity -> {
                    ResponsePayoutDetailsDto dto = transactionMapper.mapToResponsePayoutWithDetailsDto(transactionEntity);
                    dto.getCardData().maskCardNumber();
                    return dto;
                })
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .doOnError(error -> log.warn("Error retrieving payout details: {}", error.getMessage()));
    }
}
