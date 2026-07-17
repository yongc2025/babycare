package com.huigrowth.babycare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegulatoryExportRow {
    private String category;
    private String fieldName;
    private String fieldCode;
    private String value;
    private String status;
}
