package com.vitaly.fakepaymentprovider.dto.responsedto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vitaly.fakepaymentprovider.entity.util.Status;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder(toBuilder = true)
public class ResponseInProgressDto {
    private UUID transactionId;
    private Status status;
    private String message;
}
