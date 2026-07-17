package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建托育班级请求
 */
@Data
public class ClassroomCreateRequest {

    @NotNull(message = "机构ID不能为空")
    private Long organizationId;

    @NotBlank(message = "班级名称不能为空")
    @Size(min = 2, max = 50, message = "班级名称长度必须在2-50个字符之间")
    private String name;

    @Min(value = 0, message = "最小月龄不能小于0")
    private Integer ageRangeMinMonths;

    @Min(value = 0, message = "最大月龄不能小于0")
    private Integer ageRangeMaxMonths;

    @Min(value = 0, message = "托位容量不能小于0")
    private Integer capacity;
}
