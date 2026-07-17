package com.huigrowth.babycare.dto;

import com.huigrowth.babycare.entity.DataDict;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DataDictResponse {
    private Long id;
    private String dictType;
    private String dictName;
    private String itemCode;
    private String itemValue;
    private Integer sortOrder;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DataDictResponse fromEntity(DataDict d) {
        DataDictResponse r = new DataDictResponse();
        r.setId(d.getId());
        r.setDictType(d.getDictType());
        r.setDictName(d.getDictName());
        r.setItemCode(d.getItemCode());
        r.setItemValue(d.getItemValue());
        r.setSortOrder(d.getSortOrder());
        r.setStatus(d.getStatus() != null ? d.getStatus().name() : null);
        r.setRemark(d.getRemark());
        r.setCreatedAt(d.getCreatedAt());
        r.setUpdatedAt(d.getUpdatedAt());
        return r;
    }
}
