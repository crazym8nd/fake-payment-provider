package com.vitaly.fakepaymentprovider.exceptionhandling;

public class TransactionRequestInvalidPaymentMethodException extends RuntimeException{

    public TransactionRequestInvalidPaymentMethodException(String message) {
        super(message);
    }
}
