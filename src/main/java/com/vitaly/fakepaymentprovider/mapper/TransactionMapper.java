package com.vitaly.fakepaymentprovider.mapper;

import com.vitaly.fakepaymentprovider.dto.AccountDto;
import com.vitaly.fakepaymentprovider.dto.CardDto;
import com.vitaly.fakepaymentprovider.dto.TransactionDto;
import com.vitaly.fakepaymentprovider.entity.TransactionEntity;
import com.vitaly.fakepaymentprovider.service.AccountService;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    TransactionDto mapToDto(TransactionEntity transactionEntity);

    @InheritInverseConfiguration
    TransactionEntity mapFromDto(TransactionDto transactionDto);
}
