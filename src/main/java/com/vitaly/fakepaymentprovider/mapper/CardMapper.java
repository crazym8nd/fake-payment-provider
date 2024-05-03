package com.vitaly.fakepaymentprovider.mapper;

import com.vitaly.fakepaymentprovider.dto.CardDto;
import com.vitaly.fakepaymentprovider.dto.TransactionDto;
import com.vitaly.fakepaymentprovider.entity.CardEntity;
import com.vitaly.fakepaymentprovider.entity.TransactionEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = CustomerMapper.class)
public interface CardMapper {

    CardDto mapToDto(CardEntity cardEntity);

    @InheritInverseConfiguration
    CardEntity mapFromDto(CardDto cardDto);

}
