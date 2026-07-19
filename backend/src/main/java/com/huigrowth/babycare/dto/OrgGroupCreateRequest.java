package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建集团/品牌请求
 */
@Data
public class OrgGroupCreateRequest {

    @NotBlank(message = "集团名称不能为空")
    @Size(min = 2, max = 80, message = "集团名称长度必须在2-80个字符之间")
    private String name;

    @NotBlank(message = "集团编码不能为空")
    @Size(max = 50, message = "集团编码长度不能超过50个字符")
    private String code;

    @Size(max = 300, message = "简介长度不能超过300个字符")
    private String description;

    @Size(max = 500, message = "Logo URL长度不能超过500个字符")
    private String logo;

    @Size(max = 50, message = "联系人长度不能超过50个字符")
    private String contactPerson;

    @Size(max = 20, message = "联系电话长度不能超过20个字符")
    private String contactPhone;

    @Size(max = 200, message = "地址长度不能超过200个字符")
    private String address;
}
