package com.vitaly.fakepaymentprovider.service.impl;

import com.vitaly.fakepaymentprovider.entity.CustomerEntity;
import com.vitaly.fakepaymentprovider.entity.util.Status;
import com.vitaly.fakepaymentprovider.repository.CustomerRepository;
import com.vitaly.fakepaymentprovider.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    public Mono<CustomerEntity> getById(String customerId) {
        return customerRepository.findById(customerId);
    }

    @Override
    public Mono<CustomerEntity> update(CustomerEntity customerEntity) {
        return customerRepository.save(CustomerEntity.builder()
                .updatedAt(LocalDateTime.now())
                .build());
    }

    @Override
    public Mono<CustomerEntity> save(CustomerEntity customerEntity) {
        return customerRepository.save(customerEntity);
    }

    @Override
    public Mono<CustomerEntity> saveCustomerForTransaction(CustomerEntity customerEntity) {
        return customerRepository.findById(customerEntity.getCardNumber())
                .flatMap(existingCustomer -> customerRepository.save(existingCustomer.toBuilder()
                        .updatedAt(LocalDateTime.now())
                        .updatedBy("SYSTEM")
                        .build()))
                .switchIfEmpty(saveNewCustomer(customerEntity));
    }

    private Mono<CustomerEntity> saveNewCustomer(CustomerEntity customerEntity) {
        return Mono.defer(() -> {
            CustomerEntity newCustomer = customerEntity.toBuilder()
                    .createdBy("SYSTEM")
                    .status(Status.ACTIVE)
                    .build();
            return customerRepository.save(newCustomer);
        });
    }
}
