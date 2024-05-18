package com.vitaly.fakepaymentprovider.mapper;

import com.vitaly.fakepaymentprovider.dto.requestdto.RequestPayoutTransactionDto;
import com.vitaly.fakepaymentprovider.dto.requestdto.RequestTopupTransactionDto;
import com.vitaly.fakepaymentprovider.dto.responsedto.ResponsePayoutDetailsDto;
import com.vitaly.fakepaymentprovider.dto.responsedto.ResponseTopupTransactionDto;
import com.vitaly.fakepaymentprovider.dto.responsedto.ResponseTransactionDetailsDto;
import com.vitaly.fakepaymentprovider.entity.TransactionEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {CustomerMapper.class, CardMapper.class})
public interface TransactionMapper {
    ResponseTransactionDetailsDto mapToResponseTransactionWithDetailsDto(TransactionEntity transactionEntity);
    ResponsePayoutDetailsDto mapToResponsePayoutWithDetailsDto(TransactionEntity transactionEntity);

    TransactionEntity mapFromRequestTopupDto(RequestTopupTransactionDto requestTopupTransactionDto);

    TransactionEntity mapFromRequestPayoutDto(RequestPayoutTransactionDto requestPayoutTransactionDto);

}
