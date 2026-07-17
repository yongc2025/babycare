package com.huigrowth.babycare.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "fee_items", indexes = {
    @Index(name = "idx_fee_item_org", columnList = "organization_id"),
    @Index(name = "idx_fee_item_status", columnList = "status")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"organization"})
@ToString(exclude = {"organization"})
public class FeeItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Size(max = 80, message = "收费项目名称不能超过80个字符")
    @Column(name = "name", nullable = false, length = 80, columnDefinition = "VARCHAR(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String name;

    @Size(max = 300, message = "收费项目说明不能超过300个字符")
    @Column(name = "description", length = 300, columnDefinition = "VARCHAR(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String description;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FeeItemStatus status = FeeItemStatus.ACTIVE;

    public enum FeeItemStatus {
        ACTIVE("启用"),
        DISABLED("停用");

        private final String description;

        FeeItemStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
