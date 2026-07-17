package com.huigrowth.babycare.dto;

import com.huigrowth.babycare.entity.FeeItem;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FeeItemResponse {
    private Long id;
    private Long organizationId;
    private String organizationName;
    private String name;
    private String description;
    private BigDecimal amount;
    private FeeItem.FeeItemStatus status;
    private String statusDescription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
