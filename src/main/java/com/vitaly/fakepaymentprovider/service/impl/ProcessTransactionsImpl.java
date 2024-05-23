package com.vitaly.fakepaymentprovider.service.impl;

import com.vitaly.fakepaymentprovider.service.ProcessTransactions;
import com.vitaly.fakepaymentprovider.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessTransactionsImpl implements ProcessTransactions {
    private final TransactionService transactionService;

    @Override
    @Scheduled(cron = "0 * * * * *")
    public void processTopupTransactionsInProgress() {
        transactionService.processTopTransactionsInProgress(transactionService.getAllTopupTransactionsInProgress())
                .doOnSuccess(v -> log.warn("Topup transactions processed"))
                .subscribe();
    }
}
