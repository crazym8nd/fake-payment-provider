package com.vitaly.fakepaymentprovider.repository;

import com.vitaly.fakepaymentprovider.entity.CustomerEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface CustomerRepository extends R2dbcRepository<CustomerEntity, Long> {
    @Modifying
    @Query("UPDATE customers SET status = 'DELETED' WHERE id = :id")
    Mono<Void> deleteById(Long id);

    Mono<CustomerEntity> findByFirstNameAndAndLastNameAndCountry(String firstName, String lastName, String country);
}
