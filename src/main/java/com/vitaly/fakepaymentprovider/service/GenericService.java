package com.vitaly.fakepaymentprovider.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface GenericService<T, ID> {
    Flux<T> getAll();

    Mono<T> getById(ID id);

    Mono<T> update(T t);

    Mono<T> save(T t);

    Mono<T> deleteById(ID id);
}
