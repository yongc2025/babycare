package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class HealthObservationUpdateRequest {

    private LocalDate observationDate;

    private LocalDateTime observationTime;

    private String type;

    private Double temperature;

    @Size(max = 100, message = "一摸结果不能超过100个字符")
    private String touchStatus;

    @Size(max = 100, message = "二看结果不能超过100个字符")
    private String lookStatus;

    @Size(max = 100, message = "三问结果不能超过100个字符")
    private String askStatus;

    @Size(max = 100, message = "四查结果不能超过100个字符")
    private String checkStatus;

    @Size(max = 300, message = "症状描述不能超过300个字符")
    private String symptoms;

    @Size(max = 300, message = "处理建议不能超过300个字符")
    private String actionTaken;

    private Boolean abnormal;

    private Boolean followUpRequired;

    @Size(max = 30, message = "数据来源不能超过30个字符")
    private String source;
}
