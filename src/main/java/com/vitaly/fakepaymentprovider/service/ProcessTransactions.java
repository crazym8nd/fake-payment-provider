package com.vitaly.fakepaymentprovider.service;

import com.vitaly.fakepaymentprovider.entity.TransactionEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public interface ProcessTransactions {
    void processTopupTransactionsInProgress();
}
