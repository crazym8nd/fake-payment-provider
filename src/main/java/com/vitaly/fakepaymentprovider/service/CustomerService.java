package com.vitaly.fakepaymentprovider.service;

import com.vitaly.fakepaymentprovider.entity.CustomerEntity;
import reactor.core.publisher.Mono;

public interface CustomerService extends GenericService<CustomerEntity,Long>{

    Mono<CustomerEntity> getCustomerByCredentials(CustomerEntity customer);
}
