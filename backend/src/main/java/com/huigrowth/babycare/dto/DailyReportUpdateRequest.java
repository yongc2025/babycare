package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DailyReportUpdateRequest {

    @Size(max = 500, message = "日报摘要不能超过500个字符")
    private String summary;

    @Size(max = 500, message = "考勤摘要不能超过500个字符")
    private String attendanceSummary;

    @Size(max = 1000, message = "照护摘要不能超过1000个字符")
    private String careSummary;

    @Size(max = 500, message = "健康摘要不能超过500个字符")
    private String healthSummary;

    @Size(max = 500, message = "活动摘要不能超过500个字符")
    private String activitySummary;

    @Size(max = 800, message = "老师评语不能超过800个字符")
    private String teacherComment;

    @Size(max = 1200, message = "AI草稿不能超过1200个字符")
    private String aiDraftContent;
}
