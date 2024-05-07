package com.vitaly.fakepaymentprovider.mapper;

import com.vitaly.fakepaymentprovider.dto.requestdto.RequestCardDataDto;
import com.vitaly.fakepaymentprovider.dto.responsedto.ResponseCardDataDto;
import com.vitaly.fakepaymentprovider.entity.CardEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = CustomerMapper.class)
public interface CardMapper {

    ResponseCardDataDto mapToDto(CardEntity cardEntity);

    @InheritInverseConfiguration
    CardEntity mapFromDto(RequestCardDataDto requestCardDataDto);

}
