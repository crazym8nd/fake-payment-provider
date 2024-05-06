package com.vitaly.fakepaymentprovider.service.impl;

import com.vitaly.fakepaymentprovider.entity.CardEntity;
import com.vitaly.fakepaymentprovider.entity.util.Status;
import com.vitaly.fakepaymentprovider.repository.CardRepository;
import com.vitaly.fakepaymentprovider.service.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Slf4j
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;

    @Override
    public Flux<CardEntity> getAll() {
        return cardRepository.findAll();
    }

    @Override
    public Mono<CardEntity> getById(String cardNumber) {
        return cardRepository.findById(cardNumber);
    }

    @Override
    public Mono<CardEntity> update(CardEntity cardEntity) {
        return cardRepository.save(CardEntity.builder()
                .updatedAt(LocalDateTime.now())
                .build());
    }

    @Override
    public Mono<CardEntity> save(CardEntity cardEntity) {
        return cardRepository.findById(cardEntity.getCardNumber())
                .switchIfEmpty(saveNewCard(cardEntity));
    }

    @Override
    public Mono<CardEntity> deleteById(String cardNumber) {
        return cardRepository.findById(cardNumber)
                .flatMap(card -> cardRepository.deleteById(card.getCardNumber())
                        .thenReturn(card));
    }

    private Mono<CardEntity> saveNewCard(CardEntity cardEntity) {
        return Mono.defer(() -> {
            CardEntity newCard = cardEntity.toBuilder()
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .createdBy("SYSTEM")
                    .updatedBy("SYSTEM")
                    .status(Status.ACTIVE)
                    .firstTransaction(true)
                    .build();
            return cardRepository.save(newCard);
        });
    }
}
