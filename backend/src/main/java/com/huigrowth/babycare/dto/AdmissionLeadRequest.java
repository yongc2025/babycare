package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AdmissionLeadRequest {

    @NotNull(message = "机构ID不能为空")
    private Long organizationId;

    private Long intendedClassroomId;

    @NotBlank(message = "宝宝姓名不能为空")
    private String childName;

    private String childGender;

    private LocalDate childBirthday;

    @NotBlank(message = "家长姓名不能为空")
    private String guardianName;

    @NotBlank(message = "家长手机号不能为空")
    private String guardianPhone;

    private String source;

    private String intentionLevel;

    private String status;

    private LocalDate preferredStartDate;

    private String remark;
}
