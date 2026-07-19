package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class InfectiousDiseaseCreateRequest {

    @NotNull(message = "入托档案ID不能为空")
    private Long enrollmentId;

    @NotNull(message = "机构ID不能为空")
    private Long organizationId;

    @NotNull(message = "班级ID不能为空")
    private Long classroomId;

    @NotBlank(message = "疾病名称不能为空")
    @Size(max = 80, message = "疾病名称不能超过80个字符")
    private String diseaseName;

    @Size(max = 500, message = "症状描述不能超过500个字符")
    private String symptoms;

    @NotNull(message = "发病日期不能为空")
    private LocalDate onsetDate;

    @NotBlank(message = "疾病状态不能为空")
    private String status;

    @NotBlank(message = "严重程度不能为空")
    private String severity;

    @Size(max = 500, message = "处理记录不能超过500个字符")
    private String treatmentNotes;

    private Boolean parentNotified;

    @Size(max = 500, message = "密切接触者记录不能超过500个字符")
    private String closeContacts;

    private Boolean classroomAlertSent;

    @Size(max = 300, message = "备注不能超过300个字符")
    private String remark;
}
