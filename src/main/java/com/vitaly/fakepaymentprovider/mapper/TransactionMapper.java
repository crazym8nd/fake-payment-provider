package com.vitaly.fakepaymentprovider.mapper;

import com.vitaly.fakepaymentprovider.dto.RequestTransactionDto;
import com.vitaly.fakepaymentprovider.entity.TransactionEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {CustomerMapper.class, CardMapper.class})
public interface TransactionMapper {

    RequestTransactionDto mapToDto(TransactionEntity transactionEntity);

    @InheritInverseConfiguration
    TransactionEntity mapFromDto(RequestTransactionDto requestTransactionDto);
}
