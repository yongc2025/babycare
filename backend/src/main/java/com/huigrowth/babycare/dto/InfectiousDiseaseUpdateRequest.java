package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class InfectiousDiseaseUpdateRequest {

    @Size(max = 80, message = "疾病名称不能超过80个字符")
    private String diseaseName;

    @Size(max = 500, message = "症状描述不能超过500个字符")
    private String symptoms;

    private LocalDate onsetDate;

    private String status;

    private String severity;

    private LocalDate isolationStart;

    private LocalDate isolationEnd;

    private LocalDate returnDate;

    @Size(max = 500, message = "处理记录不能超过500个字符")
    private String treatmentNotes;

    private Boolean parentNotified;

    @Size(max = 500, message = "密切接触者记录不能超过500个字符")
    private String closeContacts;

    private Boolean classroomAlertSent;

    @Size(max = 300, message = "备注不能超过300个字符")
    private String remark;
}
