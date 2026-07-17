package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AnnouncementUpdateRequest {

    @Size(max = 120, message = "通知标题不能超过120个字符")
    private String title;

    @Size(max = 3000, message = "通知内容不能超过3000个字符")
    private String content;

    private Boolean requireReceipt;
}
