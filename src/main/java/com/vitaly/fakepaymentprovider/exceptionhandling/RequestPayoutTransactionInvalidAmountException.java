package com.vitaly.fakepaymentprovider.exceptionhandling;

public class RequestPayoutTransactionInvalidAmountException extends  RuntimeException{

    public RequestPayoutTransactionInvalidAmountException(String message){
        super(message);
    }
}
