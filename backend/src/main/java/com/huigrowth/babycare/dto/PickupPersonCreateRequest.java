package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PickupPersonCreateRequest {

    @NotNull(message = "入托档案ID不能为空")
    private Long enrollmentId;

    @NotBlank(message = "接送人姓名不能为空")
    @Size(max = 30, message = "接送人姓名不能超过30个字符")
    private String name;

    @Size(max = 30, message = "接送关系不能超过30个字符")
    private String relationship;

    @Size(max = 20, message = "接送人电话不能超过20个字符")
    private String phone;

    @Size(max = 80, message = "证件号不能超过80个字符")
    private String identityNo;

    @Size(max = 500, message = "照片地址不能超过500个字符")
    private String photoUrl;

    @Size(max = 300, message = "备注不能超过300个字符")
    private String remark;
}
