package com.vitaly.fakepaymentprovider.security;

import com.vitaly.fakepaymentprovider.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class MerchantDetailsService implements ReactiveUserDetailsService {
    private final MerchantRepository merchantRepository;
    @Override
    public Mono<UserDetails> findByUsername(String merchantID) {
        return merchantRepository.findByMerchantId(merchantID)
                .map(merchant -> User.withUsername(merchant.getMerchantId())
                        .password(merchant.getSecretKey())
                        .build());
    }
}
