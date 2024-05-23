package com.vitaly.fakepaymentprovider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FakePaymentProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(FakePaymentProviderApplication.class, args);
    }
    //TODO use dto response instead of map
    //TODO v dto you can make public static class for embedded dtos
    //TODO controller otdaet srazu inporgress iz servisa start transaction
    //TODO scheduled annotation job for webhooks
    //topup withdraw from customer and not giving money to merchant, and processing - after topup amount on balance - trnasaction is finished
}
