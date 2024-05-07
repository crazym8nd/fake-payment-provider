package com.vitaly.fakepaymentprovider.exceptionhandling;

import com.vitaly.fakepaymentprovider.entity.util.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RequestTopUpTransactionInvalidPaymentMethodException.class)
    public ResponseEntity<Map<String, String>> handleTopUpException() {
        String msgPaymentMethodNotAllowed = "PAYMENT_METHOD_NOT_ALLOWED";
        Map<String, String> response = new HashMap<>();
        response.put("status", Status.FAILED.toString());
        response.put("message", msgPaymentMethodNotAllowed);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    @ExceptionHandler(RequestPayoutTransactionInvalidAmountException.class)
    public ResponseEntity<Map<String, String>> handlePayoutException() {
        String msgPayoutNoMinAmount = "PAYOUT_MIN_AMOUNT";

        Map<String, String> response = new HashMap<>();
        response.put("error_code", Status.FAILED.toString());
        response.put("message", msgPayoutNoMinAmount);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

}
