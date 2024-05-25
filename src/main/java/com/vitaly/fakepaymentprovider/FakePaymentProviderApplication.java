package com.vitaly.fakepaymentprovider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FakePaymentProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(FakePaymentProviderApplication.class, args);
    }
    //TODO v dto you can make public static class for embedded dtos
}
