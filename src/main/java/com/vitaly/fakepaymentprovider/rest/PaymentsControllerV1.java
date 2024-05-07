package com.vitaly.fakepaymentprovider.rest;

import com.vitaly.fakepaymentprovider.dto.requestdto.RequestPayoutTransactionDto;
import com.vitaly.fakepaymentprovider.dto.requestdto.RequestTopupTransactionDto;
import com.vitaly.fakepaymentprovider.dto.responsedto.ResponsePayoutsListDto;
import com.vitaly.fakepaymentprovider.dto.responsedto.ResponseTransactionDetailsDto;
import com.vitaly.fakepaymentprovider.dto.responsedto.ResponseTransactionsListDto;
import com.vitaly.fakepaymentprovider.mapper.TransactionMapper;
import com.vitaly.fakepaymentprovider.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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

    //topups endpoints
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
    public Mono<ResponseEntity<ResponseTransactionsListDto>> getAllTransactionsList(
            @RequestParam(value = "start_date", required = false) Long startDate,
            @RequestParam(value = "end_date", required = false) Long endDate) {
        Flux<ResponseTransactionDetailsDto> transactionsFlux;
        if(startDate != null && endDate != null) {
            LocalDate startDateTime = LocalDate.from(LocalDateTime.ofEpochSecond(startDate, 0, ZoneOffset.UTC));
            LocalDate endDateTime = LocalDate.from(LocalDateTime.ofEpochSecond(endDate, 0, ZoneOffset.UTC));
            transactionsFlux = transactionService.getAllByPeriod(startDateTime, endDateTime)
                    .map(transactionMapper::mapToResponseWithDetailsDto)
                    .map(dto -> {
                dto.setMessage("OK");
                return dto;
            });

        } else {
            transactionsFlux = transactionService.getAll()
                    .map(transactionMapper::mapToResponseWithDetailsDto)
                    .map(dto -> {
                        dto.setMessage("OK");
                        return dto;
                    });
        }
        return transactionsFlux.collectList()
                .flatMap(list -> Mono.just(ResponseTransactionsListDto.builder()
                        .transactionList(list)
                        .build()))
                .map(responseTransactionsListDto -> ResponseEntity.ok().body(responseTransactionsListDto));
    }
    @GetMapping("/transaction/{transactionId}/details")
    public Mono<ResponseEntity<ResponseTransactionDetailsDto>> getTransactionDetails(@PathVariable UUID transactionId){
        return transactionService.getByIdWithDetails(transactionId)
                .map(transactionEntity -> {
                    ResponseTransactionDetailsDto dto = transactionMapper.mapToResponseWithDetailsDto(transactionEntity);
                    dto.setMessage("OK");
                return dto;})
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .doOnError(error -> log.warn("Error retrieving transaction details: {}", error.getMessage()));
    }

    //payout endpoints

    @PostMapping("/payout/")
    public Mono<ResponseEntity<Map<String, String>>> createPayoutTransaction(@RequestBody RequestPayoutTransactionDto payoutDto) {
        String merchantId = "PROSELYTE";
        return transactionService.processPayout(transactionMapper.mapFromRequestPayoutDto(payoutDto), merchantId)
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

   @GetMapping("/payout/list")
   public Mono<ResponseEntity<ResponsePayoutsListDto>> getAllPayoutsList(
           @RequestParam(value = "start_date", required = false) String startDateStr,
           @RequestParam(value = "end_date", required = false) String endDateStr) {
       Flux<ResponseTransactionDetailsDto> transactionsFlux;
       if(startDateStr!= null && endDateStr!= null) {
           LocalDate startDateTime = LocalDate.parse(startDateStr, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
           LocalDate endDateTime = LocalDate.parse(endDateStr, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
           transactionsFlux = transactionService.getAllByPeriod(startDateTime, endDateTime)
                   .map(transactionMapper::mapToResponseWithDetailsDto)
                   .map(dto -> {
                       dto.setMessage("OK");
                       return dto;
                   });

       } else {
           transactionsFlux = transactionService.getAll()
                   .map(transactionMapper::mapToResponseWithDetailsDto)
                   .map(dto -> {
                       dto.setMessage("OK");
                       return dto;
                   });
       }
       return transactionsFlux.collectList()
               .flatMap(list -> Mono.just(ResponsePayoutsListDto.builder()
                       .payoutList(list)
                       .build()))
               .map(responseTransactionsListDto -> ResponseEntity.ok().body(responseTransactionsListDto));
   }



    @GetMapping("/payout/{payoutId}/details")
    public Mono<ResponseEntity<ResponseTransactionDetailsDto>> getPayoutDetails(@PathVariable UUID payoutId){
        return transactionService.getByIdWithDetails(payoutId)
                .map(transactionEntity -> {
                    ResponseTransactionDetailsDto dto = transactionMapper.mapToResponseWithDetailsDto(transactionEntity);
                    dto.getCardData().maskCardNumber();
                    return dto;
                })
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .doOnError(error -> log.warn("Error retrieving payout details: {}", error.getMessage()));
    }

}
