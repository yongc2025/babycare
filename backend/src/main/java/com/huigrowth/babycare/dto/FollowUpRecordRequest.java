package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FollowUpRecordRequest {
    @NotBlank(message = "跟进内容不能为空")
    private String content;

    private LocalDateTime nextFollowUpAt;
}
