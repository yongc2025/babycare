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
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "billing_statements", indexes = {
    @Index(name = "idx_bill_enrollment", columnList = "enrollment_id"),
    @Index(name = "idx_bill_org", columnList = "organization_id"),
    @Index(name = "idx_bill_status", columnList = "status"),
    @Index(name = "idx_bill_due_date", columnList = "due_date")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"organization", "enrollment", "feeItem", "createdBy", "paidBy"})
@ToString(exclude = {"organization", "enrollment", "feeItem", "createdBy", "paidBy"})
public class BillingStatement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_item_id")
    private FeeItem feeItem;

    @Size(max = 120, message = "账单标题不能超过120个字符")
    @Column(name = "title", nullable = false, length = 120, columnDefinition = "VARCHAR(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String title;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BillingStatus status = BillingStatus.UNPAID;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Size(max = 30, message = "支付方式不能超过30个字符")
    @Column(name = "payment_method", length = 30, columnDefinition = "VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String paymentMethod;

    @Size(max = 300, message = "备注不能超过300个字符")
    @Column(name = "remark", length = 300, columnDefinition = "VARCHAR(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String remark;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by")
    private User paidBy;

    public enum BillingStatus {
        UNPAID("待支付"),
        PAID("已支付"),
        CANCELLED("已取消");

        private final String description;

        BillingStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
