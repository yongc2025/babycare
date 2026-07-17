package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HardwareEventIngestRequest {

    private Long deviceId;

    private Long organizationId;

    @Size(max = 80, message = "设备编码长度不能超过80个字符")
    private String deviceCode;

    private Long classroomId;

    private Long enrollmentId;

    private String eventType;

    private LocalDateTime eventTime;

    @Size(max = 120, message = "事件键长度不能超过120个字符")
    private String eventKey;

    @Size(max = 120, message = "识别对象引用长度不能超过120个字符")
    private String subjectRef;

    private Double confidence;

    @Size(max = 5000, message = "事件载荷长度不能超过5000个字符")
    private String payload;

    @NotNull(message = "是否仅入库不能为空")
    private Boolean rawOnly = true;
}
