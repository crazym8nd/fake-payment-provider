package com.vitaly.fakepaymentprovider.rest;

import com.vitaly.fakepaymentprovider.dto.RequestTransactionDto;
import com.vitaly.fakepaymentprovider.mapper.TransactionMapper;
import com.vitaly.fakepaymentprovider.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/payments")
@Slf4j
public class PaymentsControllerV1 {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    @GetMapping("/transaction/list")
    public Mono<ResponseEntity<Flux<RequestTransactionDto>>> getAllTransactionsList(){
        Flux<RequestTransactionDto> transactionsflux;
        transactionsflux = transactionService.getAll()
                .map(transactionMapper::mapToDto);
        return Mono.just(ResponseEntity.ok(transactionsflux));
    }
    @PostMapping("/topups/")
    public Mono<ResponseEntity<Map<String, String>>> topUpTransaction(@RequestBody RequestTransactionDto requestTransactionDto){
            return transactionService.save(
                    transactionMapper.mapFromDto(requestTransactionDto))
                    .map(savedTransaction -> {
                        Map<String, String> response = new HashMap<>();
                        response.put("transaction_id", savedTransaction.getId().toString());
                        response.put("status", savedTransaction.getStatus().toString());
                        response.put("message", "OK");
                        return response;
                    })
                    .map(ResponseEntity::ok)
                    .doOnError(error -> log.warn("Error saving transaction: {}", error.getMessage()));
    }
}
