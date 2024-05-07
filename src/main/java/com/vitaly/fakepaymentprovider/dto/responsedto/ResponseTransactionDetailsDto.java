package com.vitaly.fakepaymentprovider.dto.responsedto;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vitaly.fakepaymentprovider.entity.util.Currency;
import com.vitaly.fakepaymentprovider.entity.util.Language;
import com.vitaly.fakepaymentprovider.entity.util.Status;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder(toBuilder = true)
public class ResponseTransactionDetailsDto {
    private UUID transactionId;
    private String paymentMethod;
    private BigDecimal amount;
    private Currency currency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ResponseCardDataDto cardData;
    private Language language;
    private String notificationUrl;
    private ResponseCustomerDto customer;
    private Status status;
    private String message;
}
