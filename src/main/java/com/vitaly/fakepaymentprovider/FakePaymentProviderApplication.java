package com.vitaly.fakepaymentprovider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FakePaymentProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(FakePaymentProviderApplication.class, args);
    }
    //in top up transaction currently webhook is send with created at and updated at null
    // change maskCarnubmer in dto from dto to mapper
    //make payout transaction same as top up transaction
}
