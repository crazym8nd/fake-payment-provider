package com.vitaly.fakepaymentprovider.mapper;

import com.vitaly.fakepaymentprovider.dto.CardDto;
import com.vitaly.fakepaymentprovider.dto.CustomerDto;
import com.vitaly.fakepaymentprovider.entity.CardEntity;
import com.vitaly.fakepaymentprovider.entity.CustomerEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerDto mapToDto(CustomerEntity customerEntity);

    @InheritInverseConfiguration
    CustomerEntity mapFromDto(CustomerDto customerDto);
}
