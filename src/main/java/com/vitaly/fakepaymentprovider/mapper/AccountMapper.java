package com.vitaly.fakepaymentprovider.mapper;

import com.vitaly.fakepaymentprovider.dto.AccountDto;
import com.vitaly.fakepaymentprovider.dto.CardDto;
import com.vitaly.fakepaymentprovider.entity.AccountEntity;
import com.vitaly.fakepaymentprovider.entity.CardEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    AccountDto mapToDto(AccountEntity accountEntity);

    @InheritInverseConfiguration
    AccountEntity mapFromDto(AccountDto accountDto);

}
