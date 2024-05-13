package com.vitaly.fakepaymentprovider.util;

import com.vitaly.fakepaymentprovider.dto.requestdto.RequestCardDataDto;
import com.vitaly.fakepaymentprovider.dto.requestdto.RequestCustomerDto;
import com.vitaly.fakepaymentprovider.dto.requestdto.RequestPayoutTransactionDto;
import com.vitaly.fakepaymentprovider.dto.requestdto.RequestTopupTransactionDto;
import com.vitaly.fakepaymentprovider.entity.util.Currency;
import com.vitaly.fakepaymentprovider.entity.util.Language;

import java.math.BigDecimal;

public class TransactionDataUtils {
    //transaction dto
    public static RequestTopupTransactionDto getDtoForRequestTest(){
        return RequestTopupTransactionDto.builder()
                .paymentMethod("CARD")
                .amount(BigDecimal.valueOf(1000))
                .currency(Currency.USD)
                .cardData(
                         RequestCardDataDto.builder()
                                .cardNumber("4444555566667777")
                                .cvv("999")
                                .expDate("11/2024")
                                .build()
                 )
                .language(Language.en)
                .notificationUrl("https://test-webhook.free.beeceptor.com")
                .customer(RequestCustomerDto.builder()
                        .firstName("FirstNameTest")
                        .lastName("LastNameTest")
                        .country("CountryTest")
                        .build())
                .build();
    }

    //payout dto
    public static RequestPayoutTransactionDto getPayoutDtoForRequestTest(){
        return RequestPayoutTransactionDto.builder()
                .paymentMethod("CARD")
                .amount(BigDecimal.valueOf(10))
                .currency(Currency.USD)
                .cardData(
                        RequestCardDataDto.builder()
                                .cardNumber("1111222233334444")
                                .cvv("666")
                                .expDate("06/2026")
                                .build()
                )
                .language(Language.en)
                .notificationUrl("https://test-webhook.free.beeceptor.com")
                .customer(RequestCustomerDto.builder()
                        .firstName("FirstNamePayoutScriptTest")
                        .lastName("LastNamePayoutScriptTest")
                        .country("US")
                        .build())
                .build();
    }
}