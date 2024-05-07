package com.vitaly.fakepaymentprovider.dto.responsedto;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder(toBuilder = true)
public class ResponseTransactionsListDto {
    private List<ResponseTransactionDetailsDto> transactionList;
}
