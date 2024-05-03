package com.vitaly.fakepaymentprovider.rest;

import com.vitaly.fakepaymentprovider.dto.TransactionDto;
import com.vitaly.fakepaymentprovider.mapper.TransactionMapper;
import com.vitaly.fakepaymentprovider.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentsControllerV1 {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    @GetMapping("/transaction/list")
    public Mono<ResponseEntity<Flux<TransactionDto>>> getAllTransactionsList(){
        Flux<TransactionDto> transactionsflux;

        transactionsflux = transactionService.getAll()
                .map(transactionMapper::mapToDto);
        return Mono.just(ResponseEntity.ok(transactionsflux));
    }
}
