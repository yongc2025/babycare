package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AnnouncementCreateRequest {

    @NotNull(message = "机构ID不能为空")
    private Long organizationId;

    private Long classroomId;

    private String scope;

    @NotBlank(message = "通知标题不能为空")
    @Size(max = 120, message = "通知标题不能超过120个字符")
    private String title;

    @NotBlank(message = "通知内容不能为空")
    @Size(max = 3000, message = "通知内容不能超过3000个字符")
    private String content;

    private Boolean requireReceipt;
}
