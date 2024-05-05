package com.vitaly.fakepaymentprovider.entity;

import com.vitaly.fakepaymentprovider.entity.util.Currency;
import com.vitaly.fakepaymentprovider.entity.util.Language;
import com.vitaly.fakepaymentprovider.entity.util.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("transactions")
public class TransactionEntity implements Persistable<UUID> {
    @Id
    private UUID uuid;
    private String paymentMethod;
    private BigDecimal amount;
    private Currency currency;
    private Language language;
    private String notificationUrl;
    @Transient
    private CardEntity cardData;
    @Transient
    private CustomerEntity customer;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Status status;

    @Override
    public UUID getId() {
        return uuid;
    }
    @Override
    public boolean isNew() {
        return this.uuid == null;
    }



}
