package com.huigrowth.babycare.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "announcement_receipts", indexes = {
    @Index(name = "idx_receipt_announcement", columnList = "announcement_id"),
    @Index(name = "idx_receipt_user", columnList = "user_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_announcement_user_receipt", columnNames = {"announcement_id", "user_id"})
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"announcement", "user"})
@ToString(exclude = {"announcement", "user"})
public class AnnouncementReceipt extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "announcement_id", nullable = false)
    private Announcement announcement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "read_at", nullable = false)
    private LocalDateTime readAt;
}
