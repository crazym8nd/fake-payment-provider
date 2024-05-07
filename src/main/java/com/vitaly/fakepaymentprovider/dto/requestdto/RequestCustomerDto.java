package com.vitaly.fakepaymentprovider.dto.requestdto;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder(toBuilder = true)
public class RequestCustomerDto {
    private String firstName;
    private String lastName;
    private String country;
}
