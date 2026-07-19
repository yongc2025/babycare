package com.huigrowth.babycare.dto;

import com.huigrowth.babycare.entity.OrgGroup;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 集团/品牌响应
 */
@Data
public class OrgGroupResponse {
    private Long id;
    private String name;
    private String code;
    private String description;
    private String logo;
    private String contactPerson;
    private String contactPhone;
    private String address;
    private OrgGroup.GroupStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
