package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PickupDelegationCreateRequest {

    @NotNull(message = "入托档案ID不能为空")
    private Long enrollmentId;

    @NotNull(message = "接送日期不能为空")
    private LocalDate pickupDate;

    @NotBlank(message = "接送人姓名不能为空")
    @Size(max = 30, message = "接送人姓名不能超过30个字符")
    private String pickupPersonName;

    @Size(max = 30, message = "接送关系不能超过30个字符")
    private String pickupRelationship;

    @Size(max = 20, message = "接送人电话不能超过20个字符")
    private String pickupPhone;

    @Size(max = 300, message = "委托原因不能超过300个字符")
    private String reason;
}
