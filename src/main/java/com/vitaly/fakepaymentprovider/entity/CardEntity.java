package com.vitaly.fakepaymentprovider.entity;

import com.vitaly.fakepaymentprovider.entity.util.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("cards")
public class CardEntity implements Persistable<String> {

    @Id
    private String cardNumber;
    private String expDate;
    private String cvv;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Status status;

    @Override
    public String getId() {
        return cardNumber;
    }

    @Override
    public boolean isNew() {
        return createdAt == null;
    }
}
