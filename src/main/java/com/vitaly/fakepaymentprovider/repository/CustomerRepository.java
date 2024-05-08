package com.vitaly.fakepaymentprovider.repository;

import com.vitaly.fakepaymentprovider.entity.CustomerEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CustomerRepository extends R2dbcRepository<CustomerEntity, String> {
    @Modifying
    @Query("UPDATE customers SET status = 'DELETED' WHERE card_number = :cardNumber")
    Mono<Void> deleteById(String cardNumber);
}
