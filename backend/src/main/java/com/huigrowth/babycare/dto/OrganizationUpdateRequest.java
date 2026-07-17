package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新托育机构请求
 */
@Data
public class OrganizationUpdateRequest {

    @Size(min = 2, max = 80, message = "机构名称长度必须在2-80个字符之间")
    private String name;

    @Size(max = 300, message = "机构简介长度不能超过300个字符")
    private String description;

    @Size(max = 20, message = "联系电话长度不能超过20个字符")
    private String contactPhone;

    @Size(max = 200, message = "地址长度不能超过200个字符")
    private String address;

    @Size(max = 60, message = "备案编号长度不能超过60个字符")
    private String registrationNo;

    @Size(max = 60, message = "办学/托育许可证号长度不能超过60个字符")
    private String licenseNo;

    @Size(max = 50, message = "法定代表人长度不能超过50个字符")
    private String legalRepresentative;

    @Size(max = 100, message = "主管部门长度不能超过100个字符")
    private String supervisorDepartment;

    @Size(max = 50, message = "机构等级长度不能超过50个字符")
    private String organizationLevel;

    @Size(max = 50, message = "运营类型长度不能超过50个字符")
    private String operationType;

    private String status;
}
