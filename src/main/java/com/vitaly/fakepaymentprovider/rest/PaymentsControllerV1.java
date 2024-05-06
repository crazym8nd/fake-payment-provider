package com.vitaly.fakepaymentprovider.rest;

import com.vitaly.fakepaymentprovider.dto.requestdto.RequestPayoutTransactionDto;
import com.vitaly.fakepaymentprovider.dto.requestdto.RequestTopupTransactionDto;
import com.vitaly.fakepaymentprovider.dto.responsedto.ResponseTopupTransactionDto;
import com.vitaly.fakepaymentprovider.dto.responsedto.ResponseTransactionDetailsDto;
import com.vitaly.fakepaymentprovider.mapper.TransactionMapper;
import com.vitaly.fakepaymentprovider.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/payments")
@Slf4j
public class PaymentsControllerV1 {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    @PostMapping("/topups/")
    public Mono<ResponseEntity<Map<String, String>>> topUpTransaction(@RequestBody RequestTopupTransactionDto requestTopupTransactionDto){
        return transactionService.save(
                        transactionMapper.mapFromRequestTopupDto(requestTopupTransactionDto))
                .map(savedTransaction -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("transaction_id", savedTransaction.getTransactionId().toString());
                    response.put("status", savedTransaction.getStatus().toString());
                    response.put("message", "OK");
                    return response;
                })
                .map(ResponseEntity::ok)
                .doOnError(error -> log.warn("Error saving transaction: {}", error.getMessage()));
    }
    @GetMapping("/transaction/list")
    public Mono<ResponseEntity<Flux<ResponseTopupTransactionDto>>> getAllTransactionsList(
            @RequestParam(value = "start_date", required = false) Long startDate,
            @RequestParam(value = "end_date", required = false) Long endDate) {
        Flux<ResponseTopupTransactionDto> transactionsFlux;
        if(startDate != null && endDate != null) {
            LocalDateTime startDateTime = LocalDateTime.ofEpochSecond(startDate, 0, ZoneOffset.UTC);
            LocalDateTime endDateTime = LocalDateTime.ofEpochSecond(endDate, 0, ZoneOffset.UTC);
            transactionsFlux = transactionService.getAllByPeriod(startDateTime, endDateTime)
                    .map(transactionMapper::mapToResponseTopupDto);
        } else {
            transactionsFlux = transactionService.getAll()
                    .map(transactionMapper::mapToResponseTopupDto);
        }
        return Mono.just(ResponseEntity.ok(transactionsFlux));
    }
    @GetMapping("/transaction/{transactionId}/details")
    public Mono<ResponseEntity<ResponseTransactionDetailsDto>> getTransactionDetails(@PathVariable UUID transactionId){
        return transactionService.getByIdWithDetails(transactionId)
                .map(transactionEntity -> {
                    ResponseTransactionDetailsDto dto = transactionMapper.mapToResponseWithDetailsDto(transactionEntity);
                    dto.setStatus("APPROVED");
                    dto.setMessage("OK");
                return dto;})
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .doOnError(error -> log.warn("Error retrieving transaction details: {}", error.getMessage()));

//        return transactionService.getByIdWithDetails(transactionId)
//                .map(transactionMapper::mapToResponseWithDetailsDto)
//                .map(responseTransactionDetailsDto -> {
//                    responseTransactionDetailsDto.setDetailedStatus("APPROVED");
//                    responseTransactionDetailsDto.setMessage("OK");
//                    return responseTransactionDetailsDto;
//                })
//                .map(ResponseEntity::ok)
//                .defaultIfEmpty(ResponseEntity.notFound().build())
//                .doOnError(error -> log.warn("Error retrieving transaction details: {}", error.getMessage()));
    }



    @PostMapping("/payout/")
    public Mono<ResponseEntity<Map<String, String>>> createPayoutTransaction(@RequestBody RequestPayoutTransactionDto payoutDto) {
        return transactionService.processPayout(transactionMapper.mapFromRequestPayoutDto(payoutDto))
                .map(savedPayoutTransaction -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("transaction_id", savedPayoutTransaction.getId().toString());
                    response.put("status", savedPayoutTransaction.getStatus().toString());
                    response.put("message", "Payout is successfully completed");
                    return response;
                })
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.BAD_REQUEST).build())
                .doOnError(error -> log.warn("Error processing payout transaction: {}", error.getMessage()));
    }
}
