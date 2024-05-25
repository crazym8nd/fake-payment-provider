package com.vitaly.fakepaymentprovider.service;

import reactor.core.publisher.Mono;

public interface GenericService<T, ID> {

    Mono<T> getById(ID id);

    Mono<T> save(T t);
}
