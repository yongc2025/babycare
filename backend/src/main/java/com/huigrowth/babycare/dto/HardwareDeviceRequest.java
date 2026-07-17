package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class HardwareDeviceRequest {

    @NotNull(message = "机构ID不能为空")
    private Long organizationId;

    private Long classroomId;

    @NotBlank(message = "设备编码不能为空")
    @Size(max = 80, message = "设备编码长度不能超过80个字符")
    private String deviceCode;

    @NotBlank(message = "设备名称不能为空")
    @Size(max = 80, message = "设备名称长度不能超过80个字符")
    private String name;

    private String deviceType;

    @Size(max = 80, message = "厂商长度不能超过80个字符")
    private String vendor;

    @Size(max = 80, message = "型号长度不能超过80个字符")
    private String model;

    @Size(max = 120, message = "位置长度不能超过120个字符")
    private String location;

    @Size(max = 40, message = "接入模式长度不能超过40个字符")
    private String integrationMode;

    private String status;

    @Size(max = 300, message = "备注长度不能超过300个字符")
    private String remark;
}
