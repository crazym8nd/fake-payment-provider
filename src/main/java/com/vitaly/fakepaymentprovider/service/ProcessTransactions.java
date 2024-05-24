package com.vitaly.fakepaymentprovider.service;

import org.springframework.stereotype.Service;

@Service
public interface ProcessTransactions {
    void processTopupTransactionsInProgress();
    void processPaymentTransactionsInProgress();
}
