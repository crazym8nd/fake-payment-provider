package com.vitaly.fakepaymentprovider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FakePaymentProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(FakePaymentProviderApplication.class, args);
    }

    // In transaction i  need to make bond of customer to card so when im receving card i should return customer that attached to that card
    //then i need to add logic of retrieveing custmer to transactions and payouts
    // add security headers validation
    // make tests to add validation where its needed
}
