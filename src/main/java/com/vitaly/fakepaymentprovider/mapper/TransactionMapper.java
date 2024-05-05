package com.vitaly.fakepaymentprovider.mapper;

import com.vitaly.fakepaymentprovider.dto.RequestTransactionDto;
import com.vitaly.fakepaymentprovider.dto.ResponseTransactionDto;
import com.vitaly.fakepaymentprovider.entity.TransactionEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {CustomerMapper.class, CardMapper.class})
public interface TransactionMapper {
    ResponseTransactionDto mapToResponseDto(TransactionEntity transactionEntity);

    @InheritInverseConfiguration
    TransactionEntity mapFromRequestDto(RequestTransactionDto requestTransactionDto);
}
