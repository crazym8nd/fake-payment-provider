package com.vitaly.fakepaymentprovider.exceptionhandling;

public class InvalidWebhookDataException extends RuntimeException {
    public InvalidWebhookDataException(String message) {
        super(message);
    }
}
