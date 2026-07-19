package com.huigrowth.babycare.dto;

import lombok.Data;

/**
 * 更新家庭成员权限请求
 */
@Data
public class FamilyMemberUpdateRequest {

    private String nickname;

    private Boolean canConfirmPickup;

    private Boolean canConfirmNotification;
}
