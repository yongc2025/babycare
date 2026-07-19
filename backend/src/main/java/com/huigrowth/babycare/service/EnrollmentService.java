package com.huigrowth.babycare.service;

import com.huigrowth.babycare.dto.EnrollmentBindByCodeRequest;
import com.huigrowth.babycare.dto.EnrollmentCreateRequest;
import com.huigrowth.babycare.dto.EnrollmentGuardianRequest;
import com.huigrowth.babycare.dto.EnrollmentGuardianResponse;
import com.huigrowth.babycare.dto.EnrollmentHealthCheckRequest;
import com.huigrowth.babycare.dto.EnrollmentReviewRequest;
import com.huigrowth.babycare.dto.EnrollmentResponse;
import com.huigrowth.babycare.dto.EnrollmentSupplementRequest;
import com.huigrowth.babycare.dto.EnrollmentSupplementResponse;
import com.huigrowth.babycare.dto.EnrollmentTransferRequest;
import com.huigrowth.babycare.dto.EnrollmentUpdateRequest;
import com.huigrowth.babycare.dto.EnrollmentWithdrawRequest;
import com.huigrowth.babycare.dto.MyEnrollmentResponse;
import com.huigrowth.babycare.entity.Baby;
import com.huigrowth.babycare.entity.Classroom;
import com.huigrowth.babycare.entity.Enrollment;
import com.huigrowth.babycare.entity.EnrollmentGuardian;
import com.huigrowth.babycare.entity.EnrollmentStatusHistory;
import com.huigrowth.babycare.entity.Organization;
import com.huigrowth.babycare.entity.Staff;
import com.huigrowth.babycare.entity.User;
import com.huigrowth.babycare.exception.BusinessException;
import com.huigrowth.babycare.repository.BabyRepository;
import com.huigrowth.babycare.repository.ClassroomRepository;
import com.huigrowth.babycare.repository.EnrollmentGuardianRepository;
import com.huigrowth.babycare.repository.EnrollmentRepository;
import com.huigrowth.babycare.repository.EnrollmentStatusHistoryRepository;
import com.huigrowth.babycare.repository.FamilyMemberRepository;
import com.huigrowth.babycare.repository.OrganizationRepository;
import com.huigrowth.babycare.repository.StaffRepository;
import com.huigrowth.babycare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 宝宝入托档案服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final OrganizationRepository organizationRepository;
    private final ClassroomRepository classroomRepository;
    private final BabyRepository babyRepository;
    private final UserRepository userRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final EnrollmentGuardianRepository enrollmentGuardianRepository;
    private final EnrollmentStatusHistoryRepository enrollmentStatusHistoryRepository;
    private final StaffRepository staffRepository;

    @Transactional
    public EnrollmentResponse createEnrollment(String username, EnrollmentCreateRequest request) {
        User operator = getUser(username);
        Organization organization = getOwnedOrganization(operator, request.getOrganizationId());
        Classroom classroom = getClassroomInOrganization(request.getClassroomId(), organization);
        Baby baby = getAccessibleBaby(operator, request.getBabyId());

        boolean hasActiveEnrollment = enrollmentRepository.existsByBabyAndOrganizationAndStatusIn(
                baby,
                organization,
                List.of(Enrollment.EnrollmentStatus.PENDING, Enrollment.EnrollmentStatus.ACTIVE));
        if (hasActiveEnrollment) {
            throw new BusinessException("该宝宝已存在有效入托档案");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setBaby(baby);
        enrollment.setOrganization(organization);
        enrollment.setClassroom(classroom);
        enrollment.setStatus(Enrollment.EnrollmentStatus.PENDING);
        enrollment.setEnrolledAt(request.getEnrolledAt());
        enrollment.setAllergyNotes(request.getAllergyNotes());
        enrollment.setMedicalNotes(request.getMedicalNotes());
        enrollment.setSpecialCareNotes(request.getSpecialCareNotes());
        enrollment.setEmergencyContactName(request.getEmergencyContactName());
        enrollment.setEmergencyContactPhone(request.getEmergencyContactPhone());

        Enrollment saved = enrollmentRepository.save(enrollment);
        log.info("用户 {} 为宝宝 {} 创建入托档案", username, baby.getName());
        return convertToResponse(saved);
    }

    public List<EnrollmentResponse> getClassroomEnrollments(String username, Long classroomId) {
        User operator = getUser(username);
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new BusinessException("班级不存在"));
        getOwnedOrganization(operator, classroom.getOrganization().getId());

        return enrollmentRepository.findByClassroomOrderByCreatedAtDesc(classroom).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<EnrollmentResponse> getBabyEnrollments(String username, Long babyId) {
        User operator = getUser(username);
        Baby baby = getAccessibleBaby(operator, babyId);

        return enrollmentRepository.findByBabyOrderByCreatedAtDesc(baby).stream()
                .filter(enrollment -> canAccessOrganization(operator, enrollment.getOrganization().getId())
                        || canAccessBaby(operator, enrollment.getBaby()))
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public EnrollmentResponse getEnrollmentDetail(String username, Long enrollmentId) {
        User operator = getUser(username);
        Enrollment enrollment = getOwnedEnrollment(operator, enrollmentId);
        return convertToResponse(enrollment);
    }

    @Transactional
    public EnrollmentResponse updateEnrollment(String username, Long enrollmentId, EnrollmentUpdateRequest request) {
        User operator = getUser(username);
        Enrollment enrollment = getOwnedEnrollment(operator, enrollmentId);

        if (request.getClassroomId() != null) {
            Classroom classroom = getClassroomInOrganization(
                    request.getClassroomId(),
                    enrollment.getOrganization());
            enrollment.setClassroom(classroom);
        }
        if (StringUtils.hasText(request.getStatus())) {
            enrollment.setStatus(parseStatus(request.getStatus()));
        }
        if (request.getEnrolledAt() != null) {
            enrollment.setEnrolledAt(request.getEnrolledAt());
        }
        if (request.getAllergyNotes() != null) {
            enrollment.setAllergyNotes(request.getAllergyNotes());
        }
        if (request.getMedicalNotes() != null) {
            enrollment.setMedicalNotes(request.getMedicalNotes());
        }
        if (request.getSpecialCareNotes() != null) {
            enrollment.setSpecialCareNotes(request.getSpecialCareNotes());
        }
        if (request.getEmergencyContactName() != null) {
            enrollment.setEmergencyContactName(request.getEmergencyContactName());
        }
        if (request.getEmergencyContactPhone() != null) {
            enrollment.setEmergencyContactPhone(request.getEmergencyContactPhone());
        }

        Enrollment saved = enrollmentRepository.save(enrollment);
        log.info("用户 {} 更新入托档案: {}", username, saved.getId());
        return convertToResponse(saved);
    }

    // ========== 入托审核流程 ==========

    @Transactional
    public EnrollmentResponse reviewEnrollment(String username, Long enrollmentId, EnrollmentReviewRequest request) {
        User operator = getUser(username);
        Enrollment enrollment = getOwnedEnrollment(operator, enrollmentId);

        if (enrollment.getStatus() != Enrollment.EnrollmentStatus.PENDING) {
            throw new BusinessException("仅待入托状态的档案可以审核");
        }

        if ("APPROVE".equals(request.getAction())) {
            enrollment.setStatus(Enrollment.EnrollmentStatus.HEALTH_CHECK);
            enrollment.setReviewedBy(operator.getId());
            enrollment.setReviewedAt(java.time.LocalDateTime.now());
        } else if ("REJECT".equals(request.getAction())) {
            enrollment.setStatus(Enrollment.EnrollmentStatus.REJECTED);
            enrollment.setReviewedBy(operator.getId());
            enrollment.setReviewedAt(java.time.LocalDateTime.now());
            enrollment.setRejectReason(request.getReason());
        } else {
            throw new BusinessException("审核操作不正确，仅支持 APPROVE/REJECT");
        }

        Enrollment saved = enrollmentRepository.save(enrollment);
        log.info("用户 {} {} 入托档案: {}", username, request.getAction(), enrollmentId);
        return convertToResponse(saved);
    }

    @Transactional
    public EnrollmentResponse transferClassroom(String username, Long enrollmentId, EnrollmentTransferRequest request) {
        User operator = getUser(username);
        Enrollment enrollment = getOwnedEnrollment(operator, enrollmentId);

        if (enrollment.getStatus() != Enrollment.EnrollmentStatus.ACTIVE) {
            throw new BusinessException("仅在托状态的档案可以转班");
        }

        Classroom newClassroom = getClassroomInOrganization(request.getNewClassroomId(), enrollment.getOrganization());
        if (newClassroom.getId().equals(enrollment.getClassroom().getId())) {
            throw new BusinessException("新班级与当前班级相同");
        }

        // 记录原班级
        enrollment.setPreviousClassroomId(enrollment.getClassroom().getId());
        enrollment.setClassroom(newClassroom);
        enrollment.setTransferReason(request.getReason());

        Enrollment saved = enrollmentRepository.save(enrollment);
        log.info("用户 {} 将入托档案 {} 从班级 {} 转到 {}", username, enrollmentId,
                saved.getPreviousClassroomId(), newClassroom.getName());
        return convertToResponse(saved);
    }

    @Transactional
    public EnrollmentResponse withdrawEnrollment(String username, Long enrollmentId, EnrollmentWithdrawRequest request) {
        User operator = getUser(username);
        Enrollment enrollment = getOwnedEnrollment(operator, enrollmentId);

        if (enrollment.getStatus() != Enrollment.EnrollmentStatus.ACTIVE
                && enrollment.getStatus() != Enrollment.EnrollmentStatus.PENDING) {
            throw new BusinessException("仅待入托或在托状态的档案可以退托");
        }

        enrollment.setStatus(Enrollment.EnrollmentStatus.WITHDRAWN);
        enrollment.setWithdrawnAt(java.time.LocalDate.now());
        enrollment.setWithdrawReason(request != null ? request.getReason() : null);

        Enrollment saved = enrollmentRepository.save(enrollment);
        log.info("用户 {} 退托入托档案: {}", username, enrollmentId);
        return convertToResponse(saved);
    }

    // ========== 暂停与复托（T077） ==========

    /**
     * 暂停入托（ACTIVE → SUSPENDED）
     */
    @Transactional
    public EnrollmentResponse suspendEnrollment(String username, Long enrollmentId, String reason) {
        User operator = getUser(username);
        Enrollment enrollment = getOwnedEnrollment(operator, enrollmentId);

        if (enrollment.getStatus() != Enrollment.EnrollmentStatus.ACTIVE) {
            throw new BusinessException("仅在托状态的档案可以暂停");
        }

        enrollment.setStatus(Enrollment.EnrollmentStatus.SUSPENDED);

        Enrollment saved = enrollmentRepository.save(enrollment);

        // 记录状态变更历史
        EnrollmentStatusHistory history = new EnrollmentStatusHistory();
        history.setEnrollmentId(enrollmentId);
        history.setFromStatus(Enrollment.EnrollmentStatus.ACTIVE.name());
        history.setToStatus(Enrollment.EnrollmentStatus.SUSPENDED.name());
        history.setOperatorId(operator.getId());
        history.setOperatorName(operator.getUsername());
        history.setRemark(reason);
        enrollmentStatusHistoryRepository.save(history);

        log.info("用户 {} 暂停入托档案: {}, 原因: {}", username, enrollmentId, reason);
        return convertToResponse(saved);
    }

    /**
     * 复托（SUSPENDED → ACTIVE）
     */
    @Transactional
    public EnrollmentResponse reactivateEnrollment(String username, Long enrollmentId) {
        User operator = getUser(username);
        Enrollment enrollment = getOwnedEnrollment(operator, enrollmentId);

        if (enrollment.getStatus() != Enrollment.EnrollmentStatus.SUSPENDED) {
            throw new BusinessException("仅暂停状态的档案可以复托");
        }

        enrollment.setStatus(Enrollment.EnrollmentStatus.ACTIVE);

        Enrollment saved = enrollmentRepository.save(enrollment);

        // 记录状态变更历史
        EnrollmentStatusHistory history = new EnrollmentStatusHistory();
        history.setEnrollmentId(enrollmentId);
        history.setFromStatus(Enrollment.EnrollmentStatus.SUSPENDED.name());
        history.setToStatus(Enrollment.EnrollmentStatus.ACTIVE.name());
        history.setOperatorId(operator.getId());
        history.setOperatorName(operator.getUsername());
        history.setRemark("复托");
        enrollmentStatusHistoryRepository.save(history);

        log.info("用户 {} 复托入托档案: {}", username, enrollmentId);
        return convertToResponse(saved);
    }

    /**
     * 获取入托档案状态变更历史
     */
    public List<EnrollmentStatusHistory> getEnrollmentHistory(String username, Long enrollmentId) {
        User operator = getUser(username);
        Enrollment enrollment = getOwnedEnrollment(operator, enrollmentId);
        return enrollmentStatusHistoryRepository.findByEnrollmentIdOrderByCreatedAtAsc(enrollmentId);
    }

    // ========== 入托保健审核 ==========

    @Transactional
    public EnrollmentResponse healthCheckEnrollment(String username, Long enrollmentId, EnrollmentHealthCheckRequest request) {
        User operator = getUser(username);
        Enrollment enrollment = getOwnedEnrollment(operator, enrollmentId);

        if (enrollment.getStatus() != Enrollment.EnrollmentStatus.HEALTH_CHECK) {
            throw new BusinessException("仅保健审核中的档案可以执行此操作");
        }

        if (!isHealthWorker(operator, enrollment.getOrganization().getId())) {
            throw new BusinessException("仅保健员或保健医可以执行保健审核");
        }

        if (Boolean.TRUE.equals(request.getPassed())) {
            enrollment.setStatus(Enrollment.EnrollmentStatus.ACTIVE);
            enrollment.setReviewedBy(operator.getId());
            enrollment.setReviewedAt(java.time.LocalDateTime.now());
            if (enrollment.getEnrolledAt() == null) {
                enrollment.setEnrolledAt(java.time.LocalDate.now());
            }
        } else {
            enrollment.setStatus(Enrollment.EnrollmentStatus.REJECTED);
            enrollment.setReviewedBy(operator.getId());
            enrollment.setReviewedAt(java.time.LocalDateTime.now());
            enrollment.setRejectReason(request.getRemark());
        }

        Enrollment saved = enrollmentRepository.save(enrollment);
        log.info("保健员 {} {} 入托档案: {}", username, Boolean.TRUE.equals(request.getPassed()) ? "通过" : "驳回", enrollmentId);
        return convertToResponse(saved);
    }

    public List<EnrollmentResponse> getHealthCheckPendingEnrollments(String username, Long organizationId) {
        User operator = getUser(username);
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new BusinessException("机构不存在"));

        if (!isHealthWorker(operator, organization.getId())) {
            throw new BusinessException("仅保健员或保健医可以查看待保健审核列表");
        }

        return enrollmentRepository.findByOrganizationAndStatusOrderByCreatedAtDesc(
                organization, Enrollment.EnrollmentStatus.HEALTH_CHECK).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // ========== 家长/监护人绑定管理 ==========

    @Transactional
    public EnrollmentGuardianResponse addGuardian(String username, Long enrollmentId, EnrollmentGuardianRequest request) {
        User operator = getUser(username);
        Enrollment enrollment = getOwnedEnrollment(operator, enrollmentId);

        User guardianUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (enrollmentGuardianRepository.existsByEnrollmentAndGuardianUser(enrollment, guardianUser)) {
            throw new BusinessException("该用户已是此入托档案的监护人");
        }

        EnrollmentGuardian eg = new EnrollmentGuardian();
        eg.setEnrollment(enrollment);
        eg.setGuardianUser(guardianUser);
        eg.setRelationship(parseRelationship(request.getRelationship()));
        eg.setIsPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false);
        eg.setGuardianPhone(request.getGuardianPhone());
        eg.setRemark(request.getRemark());
        eg.setBindType(EnrollmentGuardian.BindType.DIRECT_BIND);

        EnrollmentGuardian saved = enrollmentGuardianRepository.save(eg);
        log.info("用户 {} 将用户 {} 添加为入托档案 {} 的监护人", username, guardianUser.getId(), enrollmentId);
        return convertToGuardianResponse(saved);
    }

    @Transactional
    public void removeGuardian(String username, Long enrollmentId, Long guardianId) {
        User operator = getUser(username);
        Enrollment enrollment = getOwnedEnrollment(operator, enrollmentId);

        EnrollmentGuardian eg = enrollmentGuardianRepository.findById(guardianId)
                .orElseThrow(() -> new BusinessException("监护人记录不存在"));

        if (!eg.getEnrollment().getId().equals(enrollmentId)) {
            throw new BusinessException("监护人记录不属于该入托档案");
        }

        enrollmentGuardianRepository.delete(eg);
        log.info("用户 {} 移除了入托档案 {} 的监护人 {}", username, enrollmentId, guardianId);
    }

    public List<EnrollmentGuardianResponse> getEnrollmentGuardians(String username, Long enrollmentId) {
        User operator = getUser(username);
        Enrollment enrollment = getOwnedEnrollment(operator, enrollmentId);

        return enrollmentGuardianRepository.findByEnrollmentOrderByCreatedAtAsc(enrollment).stream()
                .map(this::convertToGuardianResponse)
                .collect(Collectors.toList());
    }

    public List<MyEnrollmentResponse> getMyEnrollments(String username) {
        User currentUser = getUser(username);

        List<EnrollmentGuardian> guardianRecords = enrollmentGuardianRepository
                .findByGuardianUserOrderByCreatedAtDesc(currentUser);

        return guardianRecords.stream()
                .map(this::convertToMyEnrollmentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public EnrollmentGuardianResponse bindByInviteCode(String username, EnrollmentBindByCodeRequest request) {
        User currentUser = getUser(username);

        EnrollmentGuardian eg = enrollmentGuardianRepository.findByInviteCode(request.getInviteCode())
                .orElseThrow(() -> new BusinessException("邀请码无效或已过期"));

        if (!eg.getBindType().equals(EnrollmentGuardian.BindType.INVITE_CODE)) {
            throw new BusinessException("邀请码无效");
        }

        if (eg.getGuardianUser() != null) {
            throw new BusinessException("该邀请码已被使用");
        }

        eg.setGuardianUser(currentUser);
        eg.setRelationship(parseRelationship(request.getRelationship()));
        if (request.getIsPrimary() != null) {
            eg.setIsPrimary(request.getIsPrimary());
        }
        if (request.getGuardianPhone() != null) {
            eg.setGuardianPhone(request.getGuardianPhone());
        }

        EnrollmentGuardian saved = enrollmentGuardianRepository.save(eg);
        log.info("用户 {} 通过邀请码绑定入托档案 {}", username, saved.getEnrollment().getId());
        return convertToGuardianResponse(saved);
    }

    // ========== 邀请码生成（机构使用） ==========

    @Transactional
    public String generateInviteCode(String username, Long enrollmentId) {
        User operator = getUser(username);
        Enrollment enrollment = getOwnedEnrollment(operator, enrollmentId);

        // 检查是否已有未绑定的邀请码
        List<EnrollmentGuardian> existing = enrollmentGuardianRepository
                .findByEnrollmentOrderByCreatedAtAsc(enrollment);
        for (EnrollmentGuardian eg : existing) {
            if (eg.getBindType() == EnrollmentGuardian.BindType.INVITE_CODE
                    && eg.getGuardianUser() == null) {
                return eg.getInviteCode(); // 返回已有未使用的邀请码
            }
        }

        // 创建新的邀请码记录
        EnrollmentGuardian eg = new EnrollmentGuardian();
        eg.setEnrollment(enrollment);
        eg.setGuardianUser(null);
        eg.setRelationship(EnrollmentGuardian.GuardianRelationship.OTHER);
        eg.setIsPrimary(false);
        eg.setBindType(EnrollmentGuardian.BindType.INVITE_CODE);
        eg.setInviteCode(generateRandomCode());

        enrollmentGuardianRepository.save(eg);
        log.info("用户 {} 为入托档案 {} 生成邀请码", username, enrollmentId);
        return eg.getInviteCode();
    }

    // ========== 家长资料补充与确认（T076） ==========

    /**
     * 获取家长补充资料状态
     */
    public EnrollmentSupplementResponse getSupplementStatus(String username, Long enrollmentId) {
        User operator = getUser(username);
        Enrollment enrollment = getOwnedEnrollment(operator, enrollmentId);
        return buildSupplementResponse(enrollment, operator);
    }

    /**
     * 家长补充入托资料（宝宝身份、监护人身份、健康信息）
     */
    @Transactional
    public EnrollmentSupplementResponse saveSupplement(String username, Long enrollmentId, EnrollmentSupplementRequest request) {
        User operator = getUser(username);
        Enrollment enrollment = getOwnedEnrollment(operator, enrollmentId);

        // 仅 PENDING 或 HEALTH_CHECK 状态可补充资料
        if (enrollment.getStatus() != Enrollment.EnrollmentStatus.PENDING
                && enrollment.getStatus() != Enrollment.EnrollmentStatus.HEALTH_CHECK) {
            throw new BusinessException("仅待入托或保健审核中的档案可以补充资料");
        }

        // 更新宝宝信息
        Baby baby = enrollment.getBaby();
        boolean babyChanged = false;
        if (request.getBabyIdCard() != null) {
            baby.setIdCard(request.getBabyIdCard());
            babyChanged = true;
        }
        if (request.getBabyBirthCertificateNo() != null) {
            baby.setBirthCertificateNo(request.getBabyBirthCertificateNo());
            babyChanged = true;
        }
        if (babyChanged) {
            babyRepository.save(baby);
        }

        // 更新监护人信息（当前用户的监护人记录）
        List<EnrollmentGuardian> guardians = enrollmentGuardianRepository
                .findByEnrollmentOrderByCreatedAtAsc(enrollment);
        for (EnrollmentGuardian eg : guardians) {
            if (eg.getGuardianUser() != null && eg.getGuardianUser().getId().equals(operator.getId())) {
                boolean guardianChanged = false;
                if (request.getGuardianIdCard() != null) {
                    eg.setIdCard(request.getGuardianIdCard());
                    guardianChanged = true;
                }
                if (request.getGuardianOccupation() != null) {
                    eg.setOccupation(request.getGuardianOccupation());
                    guardianChanged = true;
                }
                if (request.getGuardianPhone() != null) {
                    eg.setGuardianPhone(request.getGuardianPhone());
                    guardianChanged = true;
                }
                if (guardianChanged) {
                    enrollmentGuardianRepository.save(eg);
                }
                break;
            }
        }

        // 更新入托健康与紧急联系信息
        boolean enrollmentChanged = false;
        if (request.getAllergyNotes() != null) {
            enrollment.setAllergyNotes(request.getAllergyNotes());
            enrollmentChanged = true;
        }
        if (request.getMedicalNotes() != null) {
            enrollment.setMedicalNotes(request.getMedicalNotes());
            enrollmentChanged = true;
        }
        if (request.getSpecialCareNotes() != null) {
            enrollment.setSpecialCareNotes(request.getSpecialCareNotes());
            enrollmentChanged = true;
        }
        if (request.getEmergencyContactName() != null) {
            enrollment.setEmergencyContactName(request.getEmergencyContactName());
            enrollmentChanged = true;
        }
        if (request.getEmergencyContactPhone() != null) {
            enrollment.setEmergencyContactPhone(request.getEmergencyContactPhone());
            enrollmentChanged = true;
        }
        if (enrollmentChanged) {
            enrollmentRepository.save(enrollment);
        }

        log.info("家长 {} 补充入托档案 {} 资料", username, enrollmentId);
        return buildSupplementResponse(enrollment, operator);
    }

    /**
     * 家长确认入托资料完整
     */
    @Transactional
    public EnrollmentSupplementResponse confirmSupplement(String username, Long enrollmentId) {
        User operator = getUser(username);
        Enrollment enrollment = getOwnedEnrollment(operator, enrollmentId);

        if (enrollment.getStatus() != Enrollment.EnrollmentStatus.PENDING
                && enrollment.getStatus() != Enrollment.EnrollmentStatus.HEALTH_CHECK) {
            throw new BusinessException("仅待入托或保健审核中的档案可以确认资料");
        }

        enrollment.setParentConfirmed(true);
        enrollment.setParentConfirmedAt(java.time.LocalDateTime.now());
        enrollmentRepository.save(enrollment);

        log.info("家长 {} 确认入托档案 {} 资料完整", username, enrollmentId);
        return buildSupplementResponse(enrollment, operator);
    }

    /**
     * 构建补充资料状态响应
     */
    private EnrollmentSupplementResponse buildSupplementResponse(Enrollment enrollment, User operator) {
        EnrollmentSupplementResponse resp = new EnrollmentSupplementResponse();
        resp.setEnrollmentId(enrollment.getId());
        resp.setStatus(enrollment.getStatus().name());
        resp.setStatusDescription(enrollment.getStatus().getDescription());

        Baby baby = enrollment.getBaby();
        resp.setBabyName(baby.getName());
        resp.setBabyGender(baby.getGender() != null ? baby.getGender().name() : null);
        resp.setBabyBirthday(baby.getBirthday() != null ? baby.getBirthday().toString() : null);
        resp.setBabyIdCard(baby.getIdCard());
        resp.setBabyBirthCertificateNo(baby.getBirthCertificateNo());
        resp.setBabyInfoFilled(baby.getIdCard() != null && !baby.getIdCard().isEmpty()
                && baby.getBirthCertificateNo() != null && !baby.getBirthCertificateNo().isEmpty());

        // 查找当前用户的监护人记录
        List<EnrollmentGuardian> guardians = enrollmentGuardianRepository
                .findByEnrollmentOrderByCreatedAtAsc(enrollment);
        for (EnrollmentGuardian eg : guardians) {
            if (eg.getGuardianUser() != null && eg.getGuardianUser().getId().equals(operator.getId())) {
                resp.setGuardianIdCard(eg.getIdCard());
                resp.setGuardianOccupation(eg.getOccupation());
                resp.setGuardianPhone(eg.getGuardianPhone());
                resp.setGuardianRelationship(eg.getRelationship().getDescription());
                resp.setGuardianInfoFilled(eg.getIdCard() != null && !eg.getIdCard().isEmpty()
                        && eg.getOccupation() != null && !eg.getOccupation().isEmpty());
                break;
            }
        }

        resp.setAllergyNotes(enrollment.getAllergyNotes());
        resp.setMedicalNotes(enrollment.getMedicalNotes());
        resp.setSpecialCareNotes(enrollment.getSpecialCareNotes());
        resp.setEmergencyContactName(enrollment.getEmergencyContactName());
        resp.setEmergencyContactPhone(enrollment.getEmergencyContactPhone());
        resp.setHealthInfoFilled(
                (enrollment.getAllergyNotes() != null && !enrollment.getAllergyNotes().isEmpty())
                || (enrollment.getMedicalNotes() != null && !enrollment.getMedicalNotes().isEmpty())
                || (enrollment.getEmergencyContactName() != null && !enrollment.getEmergencyContactName().isEmpty()));

        resp.setParentConfirmed(enrollment.getParentConfirmed());
        resp.setParentConfirmedAt(enrollment.getParentConfirmedAt());

        // 综合判定：宝宝信息 + 监护人信息 + 健康信息 都至少部分填写
        resp.setAllFilled(resp.isBabyInfoFilled() && resp.isGuardianInfoFilled() && resp.isHealthInfoFilled());

        return resp;
    }

    // ========== 私有助手方法 ==========

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    private Organization getOwnedOrganization(User user, Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new BusinessException("机构不存在"));

        if (!organizationRepository.existsByIdAndCreatedBy(organizationId, user)) {
            throw new BusinessException("您无权访问该机构");
        }

        return organization;
    }

    private Classroom getClassroomInOrganization(Long classroomId, Organization organization) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new BusinessException("班级不存在"));

        if (!classroom.getOrganization().getId().equals(organization.getId())) {
            throw new BusinessException("班级不属于该机构");
        }

        return classroom;
    }

    private Baby getAccessibleBaby(User user, Long babyId) {
        Baby baby = babyRepository.findById(babyId)
                .orElseThrow(() -> new BusinessException("宝宝不存在"));

        if (!canAccessBaby(user, baby)) {
            throw new BusinessException("您无权访问该宝宝");
        }

        return baby;
    }

    private Enrollment getOwnedEnrollment(User user, Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new BusinessException("入托档案不存在"));

        if (!canAccessOrganization(user, enrollment.getOrganization().getId())) {
            throw new BusinessException("您无权访问该入托档案");
        }

        return enrollment;
    }

    private boolean canAccessBaby(User user, Baby baby) {
        return familyMemberRepository.existsByUserAndBaby(user, baby.getFamily().getId());
    }

    private boolean canAccessOrganization(User user, Long organizationId) {
        return organizationRepository.existsByIdAndCreatedBy(organizationId, user);
    }

    private boolean isHealthWorker(User user, Long organizationId) {
        return staffRepository.existsByOrganizationIdAndUserIdAndRoleIn(
                organizationId, user.getId(),
                List.of(Staff.StaffRole.HEALTH_WORKER, Staff.StaffRole.HEALTH_DOCTOR));
    }

    private Enrollment.EnrollmentStatus parseStatus(String status) {
        try {
            return Enrollment.EnrollmentStatus.valueOf(status);
        } catch (IllegalArgumentException error) {
            throw new BusinessException("入托状态不正确");
        }
    }

    private EnrollmentGuardian.GuardianRelationship parseRelationship(String relationship) {
        try {
            return EnrollmentGuardian.GuardianRelationship.valueOf(relationship);
        } catch (IllegalArgumentException error) {
            return EnrollmentGuardian.GuardianRelationship.OTHER;
        }
    }

    private String generateRandomCode() {
        return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private EnrollmentResponse convertToResponse(Enrollment enrollment) {
        EnrollmentResponse response = new EnrollmentResponse();
        response.setId(enrollment.getId());
        response.setBabyId(enrollment.getBaby().getId());
        response.setBabyName(enrollment.getBaby().getName());
        response.setBabyGender(enrollment.getBaby().getGender());
        response.setBabyBirthday(enrollment.getBaby().getBirthday());
        response.setFamilyId(enrollment.getBaby().getFamily().getId());
        response.setOrganizationId(enrollment.getOrganization().getId());
        response.setOrganizationName(enrollment.getOrganization().getName());
        response.setClassroomId(enrollment.getClassroom().getId());
        response.setClassroomName(enrollment.getClassroom().getName());
        response.setStatus(enrollment.getStatus());
        response.setStatusDescription(enrollment.getStatus().getDescription());
        response.setEnrolledAt(enrollment.getEnrolledAt());
        response.setAllergyNotes(enrollment.getAllergyNotes());
        response.setMedicalNotes(enrollment.getMedicalNotes());
        response.setSpecialCareNotes(enrollment.getSpecialCareNotes());
        response.setEmergencyContactName(enrollment.getEmergencyContactName());
        response.setEmergencyContactPhone(enrollment.getEmergencyContactPhone());
        response.setParentConfirmed(enrollment.getParentConfirmed());
        response.setParentConfirmedAt(enrollment.getParentConfirmedAt());
        response.setBabyIdCard(enrollment.getBaby().getIdCard());
        response.setBabyBirthCertificateNo(enrollment.getBaby().getBirthCertificateNo());
        response.setCreatedAt(enrollment.getCreatedAt());
        response.setUpdatedAt(enrollment.getUpdatedAt());
        return response;
    }

    private EnrollmentGuardianResponse convertToGuardianResponse(EnrollmentGuardian eg) {
        EnrollmentGuardianResponse response = new EnrollmentGuardianResponse();
        response.setId(eg.getId());
        response.setEnrollmentId(eg.getEnrollment().getId());
        if (eg.getGuardianUser() != null) {
            response.setUserId(eg.getGuardianUser().getId());
            response.setUserName(eg.getGuardianUser().getUsername());
            response.setUserNickname(eg.getGuardianUser().getNickname());
            response.setUserPhone(eg.getGuardianUser().getPhone());
        }
        response.setRelationship(eg.getRelationship().name());
        response.setRelationshipDescription(eg.getRelationship().getDescription());
        response.setIsPrimary(eg.getIsPrimary());
        response.setIdCard(eg.getIdCard());
        response.setOccupation(eg.getOccupation());
        response.setGuardianPhone(eg.getGuardianPhone());
        response.setRemark(eg.getRemark());
        response.setBindType(eg.getBindType().name());
        response.setBindTypeDescription(eg.getBindType().getDescription());
        response.setCreatedAt(eg.getCreatedAt());
        return response;
    }

    private MyEnrollmentResponse convertToMyEnrollmentResponse(EnrollmentGuardian eg) {
        Enrollment enrollment = eg.getEnrollment();
        MyEnrollmentResponse response = new MyEnrollmentResponse();
        response.setEnrollmentId(enrollment.getId());
        response.setStatus(enrollment.getStatus().name());
        response.setStatusDescription(enrollment.getStatus().getDescription());
        response.setEnrolledAt(enrollment.getEnrolledAt());
        response.setBabyId(enrollment.getBaby().getId());
        response.setBabyName(enrollment.getBaby().getName());
        response.setBabyGender(enrollment.getBaby().getGender() != null ? enrollment.getBaby().getGender().name() : null);
        response.setBabyBirthday(enrollment.getBaby().getBirthday());
        response.setBabyIdCard(enrollment.getBaby().getIdCard());
        response.setBabyBirthCertificateNo(enrollment.getBaby().getBirthCertificateNo());
        response.setOrganizationId(enrollment.getOrganization().getId());
        response.setOrganizationName(enrollment.getOrganization().getName());
        response.setClassroomId(enrollment.getClassroom().getId());
        response.setClassroomName(enrollment.getClassroom().getName());
        response.setGuardianId(eg.getId());
        response.setRelationship(eg.getRelationship().name());
        response.setRelationshipDescription(eg.getRelationship().getDescription());
        response.setGuardianIdCard(eg.getIdCard());
        response.setGuardianOccupation(eg.getOccupation());
        response.setGuardianPhone(eg.getGuardianPhone());
        response.setIsPrimary(eg.getIsPrimary());
        response.setEmergencyContactName(enrollment.getEmergencyContactName());
        response.setEmergencyContactPhone(enrollment.getEmergencyContactPhone());
        response.setParentConfirmed(enrollment.getParentConfirmed());
        response.setParentConfirmedAt(enrollment.getParentConfirmedAt());
        response.setCreatedAt(enrollment.getCreatedAt());
        return response;
    }
}
