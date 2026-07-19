package com.huigrowth.babycare.service;

import com.huigrowth.babycare.dto.*;
import com.huigrowth.babycare.entity.*;
import com.huigrowth.babycare.exception.BusinessException;
import com.huigrowth.babycare.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 家庭管理服务
 * 
 * @author HuiGrowth Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FamilyService {

    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final UserRepository userRepository;
    private final BabyRepository babyRepository;

    /**
     * 创建家庭
     */
    @Transactional
    public FamilyResponse createFamily(String username, FamilyCreateRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 检查用户是否已经是其他家庭的创建者
        boolean hasCreatedFamily = familyMemberRepository.existsByUserAndRole(user, FamilyMember.FamilyRole.CREATOR);
        if (hasCreatedFamily) {
            throw new BusinessException("您已经创建了一个家庭，每个用户只能创建一个家庭");
        }

        // 创建家庭
        Family family = new Family();
        family.setName(request.getName());
        family.setDescription(request.getDescription());
        family.setInviteCode(generateInviteCode());
        Family savedFamily = familyRepository.save(family);

        // 添加创建者为家庭成员
        FamilyMember creator = new FamilyMember();
        creator.setUser(user);
        creator.setFamily(savedFamily);
        creator.setRole(FamilyMember.FamilyRole.CREATOR);
        creator.setNickname(user.getNickname());
        familyMemberRepository.save(creator);

        log.info("用户 {} 创建了家庭: {}", username, savedFamily.getName());

        return convertToFamilyResponse(savedFamily);
    }

    /**
     * 加入家庭
     */
    @Transactional
    public FamilyResponse joinFamily(String username, String inviteCode) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        Family family = familyRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new BusinessException("邀请码无效"));

        // 检查用户是否已经是该家庭成员
        boolean isMember = familyMemberRepository.existsByUserAndFamily(user, family);
        if (isMember) {
            throw new BusinessException("您已经是该家庭的成员");
        }

        // 检查家庭成员数量限制（最多10人）
        long memberCount = familyMemberRepository.countByFamily(family);
        if (memberCount >= 10) {
            throw new BusinessException("该家庭成员已满，最多支持10个成员");
        }

        // 添加为家庭成员
        FamilyMember member = new FamilyMember();
        member.setUser(user);
        member.setFamily(family);
        member.setRole(FamilyMember.FamilyRole.PARENT);
        member.setNickname(user.getNickname());
        familyMemberRepository.save(member);

        log.info("用户 {} 加入了家庭: {}", username, family.getName());

        return convertToFamilyResponse(family);
    }

    /**
     * 获取用户的家庭列表
     */
    public List<FamilyResponse> getUserFamilies(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        List<FamilyMember> memberships = familyMemberRepository.findByUser(user);
        
        return memberships.stream()
                .map(member -> convertToFamilyResponse(member.getFamily()))
                .collect(Collectors.toList());
    }

    /**
     * 获取家庭详情
     */
    public FamilyResponse getFamilyDetail(String username, Long familyId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new BusinessException("家庭不存在"));

        // 检查用户是否是该家庭成员
        boolean isMember = familyMemberRepository.existsByUserAndFamily(user, family);
        if (!isMember) {
            throw new BusinessException("您不是该家庭的成员");
        }

        return convertToFamilyResponse(family);
    }

    /**
     * 添加宝宝
     */
    @Transactional
    public BabyResponse addBaby(String username, Long familyId, BabyCreateRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new BusinessException("家庭不存在"));

        // 检查用户是否是该家庭成员
        boolean isMember = familyMemberRepository.existsByUserAndFamily(user, family);
        if (!isMember) {
            throw new BusinessException("您不是该家庭的成员");
        }

        // 检查宝宝数量限制（每个家庭最多5个宝宝）
        long babyCount = babyRepository.countByFamily(family);
        if (babyCount >= 5) {
            throw new BusinessException("每个家庭最多只能添加5个宝宝");
        }

        // 创建宝宝
        Baby baby = new Baby();
        baby.setName(request.getName());
        baby.setGender(Baby.Gender.valueOf(request.getGender()));
        baby.setBirthday(request.getBirthday());
        baby.setAvatar(request.getAvatar());
        baby.setFamily(family);
        Baby savedBaby = babyRepository.save(baby);

        log.info("用户 {} 在家庭 {} 中添加了宝宝: {}", username, family.getName(), savedBaby.getName());

        return convertToBabyResponse(savedBaby);
    }

    /**
     * 获取家庭宝宝列表
     */
    public List<BabyResponse> getFamilyBabies(String username, Long familyId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new BusinessException("家庭不存在"));

        // 检查用户是否是该家庭成员
        boolean isMember = familyMemberRepository.existsByUserAndFamily(user, family);
        if (!isMember) {
            throw new BusinessException("您不是该家庭的成员");
        }

        List<Baby> babies = babyRepository.findByFamily(family);
        return babies.stream()
                .map(this::convertToBabyResponse)
                .collect(Collectors.toList());
    }

    /**
     * 生成邀请码
     */
    private String generateInviteCode() {
        String characters = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // 排除容易混淆的字符
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        
        for (int i = 0; i < 6; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }
        
        // 确保邀请码唯一
        while (familyRepository.existsByInviteCode(code.toString())) {
            code = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                code.append(characters.charAt(random.nextInt(characters.length())));
            }
        }
        
        return code.toString();
    }

    /**
     * 转换为家庭响应DTO
     */
    private FamilyResponse convertToFamilyResponse(Family family) {
        FamilyResponse response = new FamilyResponse();
        response.setId(family.getId());
        response.setName(family.getName());
        response.setDescription(family.getDescription());
        response.setInviteCode(family.getInviteCode());
        response.setCreatedAt(family.getCreatedAt());
        response.setUpdatedAt(family.getUpdatedAt());

        // 获取家庭成员
        List<FamilyMember> members = familyMemberRepository.findByFamily(family);
        List<FamilyMemberResponse> memberResponses = members.stream()
                .map(this::convertToFamilyMemberResponse)
                .collect(Collectors.toList());
        response.setMembers(memberResponses);
        response.setMemberCount(memberResponses.size());

        // 获取家庭宝宝
        List<Baby> babies = babyRepository.findByFamily(family);
        List<BabyResponse> babyResponses = babies.stream()
                .map(this::convertToBabyResponse)
                .collect(Collectors.toList());
        response.setBabies(babyResponses);
        response.setBabyCount(babyResponses.size());

        return response;
    }

    /**
     * 转换为家庭成员响应DTO
     */
    private FamilyMemberResponse convertToFamilyMemberResponse(FamilyMember member) {
        FamilyMemberResponse response = new FamilyMemberResponse();
        response.setId(member.getId());
        response.setUserId(member.getUser().getId());
        response.setUsername(member.getUser().getUsername());
        response.setNickname(member.getNickname());
        response.setAvatar(member.getUser().getAvatar());
        response.setRole(member.getRole());
        response.setRoleDescription(member.getRole().getDescription());
        response.setCanConfirmPickup(member.getCanConfirmPickup());
        response.setCanConfirmNotification(member.getCanConfirmNotification());
        response.setJoinedAt(member.getCreatedAt());
        return response;
    }

    /**
     * 更新家庭成员权限（T073）
     */
    @Transactional
    public FamilyMemberResponse updateMemberPermissions(String username, Long familyId, Long memberId, FamilyMemberUpdateRequest request) {
        User operator = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new BusinessException("家庭不存在"));

        // 检查操作者是否是家庭成员（且是创建者或家长角色）
        FamilyMember operatorMember = familyMemberRepository.findByUserAndFamily(operator, family);
        if (operatorMember == null) {
            throw new BusinessException("您不是该家庭的成员");
        }

        if (operatorMember.getRole() != FamilyMember.FamilyRole.CREATOR
                && operatorMember.getRole() != FamilyMember.FamilyRole.PARENT) {
            throw new BusinessException("仅创建者或家长可以管理成员权限");
        }

        FamilyMember member = familyMemberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException("家庭成员不存在"));

        if (!member.getFamily().getId().equals(familyId)) {
            throw new BusinessException("该成员不属于此家庭");
        }

        if (request.getNickname() != null) {
            member.setNickname(request.getNickname());
        }
        if (request.getCanConfirmPickup() != null) {
            member.setCanConfirmPickup(request.getCanConfirmPickup());
        }
        if (request.getCanConfirmNotification() != null) {
            member.setCanConfirmNotification(request.getCanConfirmNotification());
        }

        FamilyMember saved = familyMemberRepository.save(member);
        log.info("用户 {} 更新了家庭成员 {} 的权限", username, memberId);
        return convertToFamilyMemberResponse(saved);
    }

    /**
     * 转换为宝宝响应DTO
     */
    private BabyResponse convertToBabyResponse(Baby baby) {
        BabyResponse response = new BabyResponse();
        response.setId(baby.getId());
        response.setName(baby.getName());
        response.setGender(baby.getGender());
        response.setBirthday(baby.getBirthday());
        response.setAvatar(baby.getAvatar());
        response.setFamilyId(baby.getFamily().getId());
        response.setCreatedAt(baby.getCreatedAt());

        // 计算年龄
        long ageInDays = ChronoUnit.DAYS.between(baby.getBirthday(), LocalDate.now());
        response.setAgeInDays((int) ageInDays);
        response.setAgeDescription(formatAge(ageInDays));

        return response;
    }

    /**
     * 格式化年龄描述
     */
    private String formatAge(long ageInDays) {
        if (ageInDays < 30) {
            return ageInDays + "天";
        } else if (ageInDays < 365) {
            long months = ageInDays / 30;
            long days = ageInDays % 30;
            return months + "个月" + (days > 0 ? days + "天" : "");
        } else {
            long years = ageInDays / 365;
            long months = (ageInDays % 365) / 30;
            return years + "岁" + (months > 0 ? months + "个月" : "");
        }
    }
}