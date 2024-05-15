package com.vitaly.fakepaymentprovider.mapper;

import com.vitaly.fakepaymentprovider.dto.requestdto.RequestCustomerDto;
import com.vitaly.fakepaymentprovider.dto.responsedto.ResponseCustomerDto;
import com.vitaly.fakepaymentprovider.dto.webhook.WebhookResponseCustomerDto;
import com.vitaly.fakepaymentprovider.entity.CustomerEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    ResponseCustomerDto mapToDto(CustomerEntity customerEntity);

    @InheritInverseConfiguration
    CustomerEntity mapFromDto(RequestCustomerDto requestCustomerDto);

    WebhookResponseCustomerDto mapToWebhookCustomerDto(CustomerEntity customerEntity);
}
