package com.vitaly.fakepaymentprovider.util;

import com.vitaly.fakepaymentprovider.entity.TransactionEntity;
import com.vitaly.fakepaymentprovider.entity.util.Currency;
import com.vitaly.fakepaymentprovider.entity.util.Language;

import java.math.BigDecimal;

public class TransactionDataUtils {
    //transaction entities
    public static TransactionEntity transactionForDetailsTest(){
        return  TransactionEntity.builder()
                .paymentMethod("CARD")
                .amount(BigDecimal.valueOf(10000))
                .currency(Currency.USD)
                .language(Language.en)
                .notificationUrl("https://proselyte.net/webhook/transaction")
                .cardData(null)
                .customer(null)
                .build();
    }
}