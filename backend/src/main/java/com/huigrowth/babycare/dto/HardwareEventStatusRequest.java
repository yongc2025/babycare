package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class HardwareEventStatusRequest {

    @NotBlank(message = "事件状态不能为空")
    private String status;

    @Size(max = 500, message = "处理备注长度不能超过500个字符")
    private String processRemark;
}
