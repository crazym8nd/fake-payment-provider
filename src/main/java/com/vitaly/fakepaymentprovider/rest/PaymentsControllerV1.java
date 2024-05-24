package com.vitaly.fakepaymentprovider.rest;

import com.vitaly.fakepaymentprovider.dto.requestdto.RequestPayoutTransactionDto;
import com.vitaly.fakepaymentprovider.dto.requestdto.RequestTopupTransactionDto;
import com.vitaly.fakepaymentprovider.dto.responsedto.*;
import com.vitaly.fakepaymentprovider.entity.AccountEntity;
import com.vitaly.fakepaymentprovider.entity.CardEntity;
import com.vitaly.fakepaymentprovider.entity.CustomerEntity;
import com.vitaly.fakepaymentprovider.entity.TransactionEntity;
import com.vitaly.fakepaymentprovider.entity.util.Status;
import com.vitaly.fakepaymentprovider.entity.util.TransactionType;
import com.vitaly.fakepaymentprovider.mapper.TransactionMapper;
import com.vitaly.fakepaymentprovider.service.AccountService;
import com.vitaly.fakepaymentprovider.service.CardService;
import com.vitaly.fakepaymentprovider.service.CustomerService;
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
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/payments")
@Slf4j
public class PaymentsControllerV1 {
    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;
    private final CardService cardService;
    private final CustomerService customerService;
    private final AccountService accountService;

    //topups endpoints
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/topups/")
    public Mono<ResponseEntity<ResponseInProgressDto>> processTopupTransaction(@RequestBody RequestTopupTransactionDto requestTopupTransactionDto, Authentication authentication){
        return transactionService.validateTopupTransaction(transactionMapper.mapFromRequestTopupDto(requestTopupTransactionDto).toBuilder()
                        .transactionType(TransactionType.TOPUP).build())
                .flatMap(validatedTransaction -> {
                    ResponseEntity<ResponseInProgressDto> response = ResponseEntity.ok(
                            ResponseInProgressDto.builder()
                                    .transactionId(validatedTransaction.getTransactionId())
                                    .status(validatedTransaction.getStatus())
                                    .message("OK")
                                    .build());

                    Mono<CardEntity> saveCardData = cardService.saveCardForTransaction(validatedTransaction.getCardData());
                    Mono<CustomerEntity> saveCustomerData = customerService.saveCustomerForTransaction(CustomerEntity.builder()
                            .firstName(validatedTransaction.getCustomer().getFirstName())
                            .lastName(validatedTransaction.getCustomer().getLastName())
                            .country(validatedTransaction.getCustomer().getCountry())
                            .cardNumber(validatedTransaction.getCardData().getCardNumber())
                            .build());
                    Mono<AccountEntity> saveAccountData = accountService.saveAccountForTransaction(AccountEntity.builder()
                            .merchantId(authentication.getName())
                            .currency(validatedTransaction.getCurrency())
                            .amount(validatedTransaction.getAmount())
                            .build());

                    return Mono.when(saveCardData, saveCustomerData, saveAccountData)
                            .then(Mono.zip(saveCardData, saveCustomerData, saveAccountData))
                            .flatMap(tuple -> {
                                CardEntity savedCard = tuple.getT1();
                                CustomerEntity savedCustomer = tuple.getT2();
                                AccountEntity savedAccount = tuple.getT3();

                                validatedTransaction.setCardNumber(savedCard.getCardNumber());
                                validatedTransaction.setCustomer(savedCustomer);
                                validatedTransaction.setAccountId(savedAccount.getId());
                                return transactionService.save(validatedTransaction);
                            })
                            .thenReturn(response);
                });
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
    public Mono<ResponseEntity<ResponseInProgressDto>> processPayoutTransaction(@RequestBody RequestPayoutTransactionDto payoutDto, Authentication authentication) {
       return transactionService.validatePayoutTransaction(transactionMapper.mapFromRequestPayoutDto(payoutDto).toBuilder()
               .transactionType(TransactionType.PAYOUT).build(), authentication.getName())
               .flatMap(validatedTransaction -> {
                   ResponseEntity<ResponseInProgressDto> response = ResponseEntity.ok(
                           ResponseInProgressDto.builder()
                                   .transactionId(validatedTransaction.getTransactionId())
                                   .status(Status.SUCCESS)
                                   .message("Payout is successfully completed")
                                   .build());

                   Mono<CardEntity> saveCardData = cardService.saveCardForTransaction(validatedTransaction.getCardData());
                   Mono<CustomerEntity> saveCustomerData = customerService.saveCustomerForTransaction(CustomerEntity.builder()
                           .firstName(validatedTransaction.getCustomer().getFirstName())
                           .lastName(validatedTransaction.getCustomer().getLastName())
                           .country(validatedTransaction.getCustomer().getCountry())
                           .cardNumber(validatedTransaction.getCardData().getCardNumber())
                           .build());
                   Mono<AccountEntity> saveAccountData = accountService.saveAccountForTransaction(AccountEntity.builder()
                           .merchantId(authentication.getName())
                           .currency(validatedTransaction.getCurrency())
                           .amount(validatedTransaction.getAmount())
                           .build());

                   return Mono.when(saveCardData, saveCustomerData, saveAccountData)
                           .then(Mono.zip(saveCardData, saveCustomerData, saveAccountData))
                           .flatMap(tuple -> {
                               CardEntity savedCard = tuple.getT1();
                               CustomerEntity savedCustomer = tuple.getT2();
                               AccountEntity savedAccount = tuple.getT3();

                               validatedTransaction.setCardNumber(savedCard.getCardNumber());
                               validatedTransaction.setCustomer(savedCustomer);
                               validatedTransaction.setAccountId(savedAccount.getId());
                               return transactionService.save(validatedTransaction);
                           })
                           .thenReturn(response);
               });

//        return transactionService.validatePayoutTransaction(transactionMapper.mapFromRequestPayoutDto(payoutDto).toBuilder()
//                        .transactionType(TransactionType.PAYOUT).build(), authentication.getName())
//                .map(validatedTransaction -> ResponseInProgressDto.builder()
//                        .transactionId(validatedTransaction.getTransactionId())
//                        .status(Status.SUCCESS)
//                        .message("Payout is successfully completed")
//                        .build())
//                .map(ResponseEntity::ok);
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
