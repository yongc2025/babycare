package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DataDictCreateRequest {
    @NotBlank(message = "字典类型不能为空")
    @Size(max = 100, message = "字典类型最长100个字符")
    private String dictType;

    @NotBlank(message = "字典名称不能为空")
    @Size(max = 100, message = "字典名称最长100个字符")
    private String dictName;

    @NotBlank(message = "字典项编码不能为空")
    @Size(max = 100, message = "字典项编码最长100个字符")
    private String itemCode;

    @NotBlank(message = "字典项值不能为空")
    @Size(max = 255, message = "字典项值最长255个字符")
    private String itemValue;

    private Integer sortOrder;
    private String remark;
}
