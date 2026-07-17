package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CareRecordCreateRequest {

    @NotNull(message = "入托档案ID不能为空")
    private Long enrollmentId;

    private LocalDate recordDate;

    private LocalDateTime recordTime;

    @NotBlank(message = "照护类型不能为空")
    private String type;

    @Size(max = 100, message = "记录值不能超过100个字符")
    private String valueText;

    private Double amount;

    @Size(max = 20, message = "单位不能超过20个字符")
    private String unit;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    @Size(max = 300, message = "备注不能超过300个字符")
    private String remark;

    @Size(max = 30, message = "数据来源不能超过30个字符")
    private String source;
}
