package com.vitaly.fakepaymentprovider.mapper;

import com.vitaly.fakepaymentprovider.dto.CardDto;
import com.vitaly.fakepaymentprovider.dto.MerchantDto;
import com.vitaly.fakepaymentprovider.entity.CardEntity;
import com.vitaly.fakepaymentprovider.entity.MerchantEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {AccountMapper.class})
public interface MerchantMapper {

    MerchantDto mapToDto(MerchantEntity merchantEntity);

    @InheritInverseConfiguration
    MerchantEntity mapFromDto(MerchantDto merchantDto);

}
