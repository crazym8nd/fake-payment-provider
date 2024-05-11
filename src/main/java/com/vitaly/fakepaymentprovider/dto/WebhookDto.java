package com.vitaly.fakepaymentprovider.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vitaly.fakepaymentprovider.dto.responsedto.ResponseCardDataDto;
import com.vitaly.fakepaymentprovider.dto.responsedto.ResponseCustomerDto;
import com.vitaly.fakepaymentprovider.entity.util.Language;
import com.vitaly.fakepaymentprovider.entity.util.Status;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.UUID;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder(toBuilder = true)
public class WebhookDto {
    private UUID transactionId;
    private String paymentMethod;
    private BigDecimal amount;
    private Currency currency;
    private String  type;
    private String externalTransactionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ResponseCardDataDto cardData;
    private Language language;
    private ResponseCustomerDto customer; // no country
    private Status status;
    private String message;
}
