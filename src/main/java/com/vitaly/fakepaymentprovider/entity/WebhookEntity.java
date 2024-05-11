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
import java.util.Objects;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("webhooks")
public class WebhookEntity implements Persistable<Long> {

    @Id
    private Long id;
    private UUID transactionId;
    private Long transactionAttempt;
    private String urlRequest;
    private String bodyRequest;
    private String message;
    private String bodyResponse;
    private String statusResponse;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Status status;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return Objects.isNull(id);
    }
}
