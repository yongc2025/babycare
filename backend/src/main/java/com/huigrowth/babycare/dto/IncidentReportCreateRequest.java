package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IncidentReportCreateRequest {

    @NotNull(message = "入托档案ID不能为空")
    private Long enrollmentId;

    @NotBlank(message = "事件类型不能为空")
    private String type;

    private String severity;

    private LocalDateTime occurredAt;

    @Size(max = 80, message = "发生地点不能超过80个字符")
    private String location;

    @NotBlank(message = "标题不能为空")
    @Size(max = 120, message = "标题不能超过120个字符")
    private String title;

    @Size(max = 1000, message = "事故描述不能超过1000个字符")
    private String description;

    @Size(max = 1000, message = "处理过程不能超过1000个字符")
    private String handlingProcess;

    @Size(max = 500, message = "后续跟进不能超过500个字符")
    private String followUpPlan;
}
