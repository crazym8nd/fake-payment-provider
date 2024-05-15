package com.vitaly.fakepaymentprovider.mapper;

import com.vitaly.fakepaymentprovider.dto.requestdto.RequestCardDataDto;
import com.vitaly.fakepaymentprovider.dto.responsedto.ResponseCardDataDto;
import com.vitaly.fakepaymentprovider.dto.webhook.WebhookResponseCardDataDto;
import com.vitaly.fakepaymentprovider.entity.CardEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = CustomerMapper.class)
public interface CardMapper {

    ResponseCardDataDto mapToDto(CardEntity cardEntity);

    @InheritInverseConfiguration
    CardEntity mapFromDto(RequestCardDataDto requestCardDataDto);

    @Mapping(target = "cardNumber", qualifiedByName = "maskCardData")
    WebhookResponseCardDataDto mapToWebhookCardDataDto(CardEntity cardEntity);

    @Named("maskCardData")
    default String maskCardData(String cardNumber) {
        return cardNumber.substring(0, 4) + "***" + cardNumber.substring(12);
    }

}
