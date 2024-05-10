package com.vitaly.fakepaymentprovider.entity;


import com.vitaly.fakepaymentprovider.entity.util.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("merchants")
public class MerchantEntity implements Persistable<String> {

    @Id
    private String merchantId;
    private String secretKey;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Status status;

    @Transient
    private List<AccountEntity> accountEntities;

    @Override
    public String getId() {
        return merchantId;
    }
    @Override
    public boolean isNew() {
        return Objects.isNull(merchantId);
    }

}
