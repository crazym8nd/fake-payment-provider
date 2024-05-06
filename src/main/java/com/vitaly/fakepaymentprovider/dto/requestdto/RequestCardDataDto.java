package com.vitaly.fakepaymentprovider.dto.requestdto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder(toBuilder = true)
public class RequestCardDataDto {
    private String cardNumber;
    private String expDate;
    private String cvv;
}
