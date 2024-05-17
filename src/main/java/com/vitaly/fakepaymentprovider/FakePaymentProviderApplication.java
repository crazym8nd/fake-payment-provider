package com.vitaly.fakepaymentprovider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FakePaymentProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(FakePaymentProviderApplication.class, args);
    }
    // change account logic to work with account id instead of currency and merchant id
    // factory for dto?
}
