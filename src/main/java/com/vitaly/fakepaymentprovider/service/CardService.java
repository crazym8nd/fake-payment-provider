package com.vitaly.fakepaymentprovider.service;

import com.vitaly.fakepaymentprovider.entity.CardEntity;
import reactor.core.publisher.Mono;

public interface CardService extends GenericService<CardEntity,String>{

    Mono<CardEntity> saveCardInTransaction(CardEntity cardEntity);
}
