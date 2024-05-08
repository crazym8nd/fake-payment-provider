package com.vitaly.fakepaymentprovider.service;

import com.vitaly.fakepaymentprovider.entity.CustomerEntity;
import reactor.core.publisher.Mono;

public interface CustomerService extends GenericService<CustomerEntity,String>{
    Mono<CustomerEntity> saveCustomerInTransaction(CustomerEntity customerEntity);
}
