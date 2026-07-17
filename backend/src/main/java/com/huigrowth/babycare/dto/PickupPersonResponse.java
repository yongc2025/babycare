package com.huigrowth.babycare.dto;

import com.huigrowth.babycare.entity.AuthorizedPickupPerson;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PickupPersonResponse {
    private Long id;
    private Long enrollmentId;
    private Long babyId;
    private String babyName;
    private Long classroomId;
    private String classroomName;
    private Long organizationId;
    private String organizationName;
    private String name;
    private String relationship;
    private String phone;
    private String identityNo;
    private String photoUrl;
    private AuthorizedPickupPerson.PickupPersonStatus status;
    private String statusDescription;
    private String remark;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
