package com.vitaly.fakepaymentprovider.dto.responsedto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder(toBuilder = true)
public class ResponseCardDataDto {
    private String cardNumber;

    public String maskCardNumber(){
        String maskedCardNumber = cardNumber.substring(0, 4) + "***" + cardNumber.substring(12);
        setCardNumber(maskedCardNumber);
        return maskedCardNumber;
    }
}
