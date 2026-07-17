package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ChildDevelopmentAssessmentRequest {

    @NotNull(message = "入托档案ID不能为空")
    private Long enrollmentId;

    @NotNull(message = "评估日期不能为空")
    private LocalDate assessmentDate;

    @NotNull(message = "宝宝月龄不能为空")
    private Integer childAgeMonths;

    @NotBlank(message = "评估模式不能为空")
    private String assessmentMode;

    @NotBlank(message = "评估标题不能为空")
    private String title;

    private Integer grossMotorScore;
    private Integer fineMotorScore;
    private Integer languageScore;
    private Integer cognitiveScore;
    private Integer socialEmotionalScore;
    private Integer healthScore;
    private Integer scienceScore;
    private Integer artScore;
    private Integer maxScore;
    private String overallLevel;
    private String summary;
    private String recommendation;
    private String radarData;
}
