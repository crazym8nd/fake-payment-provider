package com.vitaly.fakepaymentprovider.dto.requestdto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vitaly.fakepaymentprovider.entity.util.Currency;
import com.vitaly.fakepaymentprovider.entity.util.Language;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder(toBuilder = true)
public class RequestPayoutTransactionDto {
    private String paymentMethod;
    private BigDecimal amount;
    private Currency currency;
    private RequestCardDataDto cardData;
    private Language language;
    private String notificationUrl;
    private RequestCustomerDto customer;
}
