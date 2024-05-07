package com.vitaly.fakepaymentprovider.service.impl;

import com.vitaly.fakepaymentprovider.entity.CustomerEntity;
import com.vitaly.fakepaymentprovider.entity.util.Status;
import com.vitaly.fakepaymentprovider.repository.CustomerRepository;
import com.vitaly.fakepaymentprovider.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    public Flux<CustomerEntity> getAll() {
        return customerRepository.findAll();
    }

    @Override
    public Mono<CustomerEntity> getById(Long customerId) {
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
        return customerRepository.save(customerEntity.toBuilder()
                        .firstName(customerEntity.getFirstName())
                        .lastName(customerEntity.getLastName())
                        .country(customerEntity.getCountry())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .createdBy("SYSTEM")
                        .updatedBy("SYSTEM")
                        .status(Status.ACTIVE)
                .build());
    }

    @Override
    public Mono<CustomerEntity> deleteById(Long customerId) {
        return customerRepository.findById(customerId)
                .flatMap(customer -> customerRepository.deleteById(customerId)
                        .thenReturn(customer));
    }

    @Override
    public Mono<CustomerEntity> getCustomerByCredentials(CustomerEntity customer) {
        return customerRepository.findByFirstNameAndAndLastNameAndCountry(customer.getFirstName(),
                customer.getLastName(), customer.getCountry());
    }
}
