package com.vitaly.fakepaymentprovider.exceptionhandling;

public class RequestTopUpTransactionInvalidPaymentMethodException extends RuntimeException{

    public RequestTopUpTransactionInvalidPaymentMethodException(String message) {
        super(message);
    }
}
