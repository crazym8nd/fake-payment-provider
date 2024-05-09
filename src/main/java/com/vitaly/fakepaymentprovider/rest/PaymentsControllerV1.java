package com.vitaly.fakepaymentprovider.rest;

import com.vitaly.fakepaymentprovider.dto.requestdto.RequestPayoutTransactionDto;
import com.vitaly.fakepaymentprovider.dto.requestdto.RequestTopupTransactionDto;
import com.vitaly.fakepaymentprovider.dto.responsedto.ResponsePayoutsListDto;
import com.vitaly.fakepaymentprovider.dto.responsedto.ResponseTransactionDetailsDto;
import com.vitaly.fakepaymentprovider.dto.responsedto.ResponseTransactionsListDto;
import com.vitaly.fakepaymentprovider.entity.util.TransactionType;
import com.vitaly.fakepaymentprovider.mapper.TransactionMapper;
import com.vitaly.fakepaymentprovider.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.*;
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
    @PostMapping("/topups/")
    public Mono<ResponseEntity<Map<String, String>>> topUpTransaction(@RequestBody RequestTopupTransactionDto requestTopupTransactionDto){

        return transactionService.processTopupTransaction(
                        transactionMapper.mapFromRequestTopupDto(requestTopupTransactionDto))
                .map(savedTransaction -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("transaction_id", savedTransaction.getTransactionId().toString());
                    response.put("status", savedTransaction.getStatus().toString());
                    response.put("message", "OK");
                    return response;
                })
                .map(ResponseEntity::ok);
    }
    @GetMapping("/transaction/list")
    public Mono<ResponseEntity<ResponseTransactionsListDto>> getAllTransactionsList(
            @RequestParam(value = "start_date", required = false) Long startDateUnix,
            @RequestParam(value = "end_date", required = false) Long endDateUnix) {
        LocalDateTime startDate;
        LocalDateTime endDate;
        if(startDateUnix!=null & startDateUnix!=null){
            startDate = Instant.ofEpochSecond(startDateUnix).atZone(ZoneOffset.UTC).toLocalDateTime();
            endDate = Instant.ofEpochSecond(endDateUnix).atZone(ZoneOffset.UTC).toLocalDateTime();
            return transactionService.getAllTransactionsByTypeAndPeriod(TransactionType.TOPUP, startDate, endDate)
                    .collectList()
                    .flatMap(list -> Mono.just(ResponseTransactionsListDto.builder()
                            .transactionList(list.stream()
                                    .map(transactionMapper::mapToResponseWithDetailsDto)
                                    .peek(dto -> dto.setMessage("OK"))
                                    .collect(Collectors.toList()))
                            .build()))
                    .map(ResponseEntity::ok);
        } else {
            LocalDate today = LocalDate.now(ZoneOffset.UTC);
            return transactionService.getAllTransactionsByTypeAndDay(TransactionType.TOPUP, today)
                    .collectList()
                    .flatMap(list -> Mono.just(ResponseTransactionsListDto.builder()
                            .transactionList(list.stream()
                                    .map(transactionMapper::mapToResponseWithDetailsDto)
                                    .peek(dto -> dto.setMessage("OK"))
                                    .collect(Collectors.toList()))
                            .build()))
                    .map(ResponseEntity::ok);
        }

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
        return transactionService.processPayoutTransaction(transactionMapper.mapFromRequestPayoutDto(payoutDto), merchantId)
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
           @RequestParam(value = "start_date", required = false)  String startDateStr,
           @RequestParam(value = "end_date", required = false)  String endDateStr) {
       Flux<ResponseTransactionDetailsDto> transactionsFlux;
       DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

       if(startDateStr!= null && endDateStr!= null) {
           LocalDateTime startDateTime = LocalDate.parse(startDateStr, formatter).atStartOfDay(ZoneOffset.UTC).toLocalDateTime();
           LocalDateTime endDateTime = LocalDate.parse(endDateStr, formatter).atTime(23, 59, 59).atZone(ZoneOffset.UTC).toLocalDateTime();
           transactionsFlux = transactionService.getAllTransactionsByTypeAndPeriod(TransactionType.PAYOUT, startDateTime, endDateTime)
                   .map(transactionMapper::mapToResponseWithDetailsDto)
                   .map(dto -> {
                       dto.setMessage("OK");
                       return dto;
                   });

       } else {
           LocalDate today = LocalDate.now(ZoneOffset.UTC);
           transactionsFlux = transactionService.getAllTransactionsByTypeAndDay(TransactionType.PAYOUT, today)
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
