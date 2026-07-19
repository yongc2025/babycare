package com.huigrowth.babycare.dto;

import com.huigrowth.babycare.entity.FamilyMember;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 家庭成员响应DTO
 * 
 * @author HuiGrowth Team
 */
@Data
public class FamilyMemberResponse {
    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private FamilyMember.FamilyRole role;
    private String roleDescription;
    private Boolean canConfirmPickup;
    private Boolean canConfirmNotification;
    private LocalDateTime joinedAt;
}