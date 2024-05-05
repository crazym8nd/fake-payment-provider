package com.vitaly.fakepaymentprovider.mapper;

import com.vitaly.fakepaymentprovider.dto.WebhookDto;
import com.vitaly.fakepaymentprovider.entity.WebhookEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WebhookMapper {

    WebhookDto mapToDto(WebhookEntity webhookEntity);

    @InheritInverseConfiguration
    WebhookEntity mapFromDto(WebhookDto webhookDto);
}
