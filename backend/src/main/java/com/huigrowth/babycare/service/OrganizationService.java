package com.huigrowth.babycare.service;

import com.huigrowth.babycare.dto.OrganizationCreateRequest;
import com.huigrowth.babycare.dto.OrganizationResponse;
import com.huigrowth.babycare.dto.OrganizationUpdateRequest;
import com.huigrowth.babycare.entity.OrgGroup;
import com.huigrowth.babycare.entity.Organization;
import com.huigrowth.babycare.entity.Staff;
import com.huigrowth.babycare.entity.User;
import com.huigrowth.babycare.exception.BusinessException;
import com.huigrowth.babycare.repository.OrgGroupRepository;
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
 * 托育机构服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final OrgGroupRepository orgGroupRepository;
    private final StaffRepository staffRepository;

    @Transactional
    public OrganizationResponse createOrganization(String username, OrganizationCreateRequest request) {
        User user = getUser(username);

        Organization organization = new Organization();
        organization.setName(request.getName());
        organization.setDescription(request.getDescription());
        organization.setContactPhone(request.getContactPhone());
        organization.setAddress(request.getAddress());
        organization.setRegistrationNo(request.getRegistrationNo());
        organization.setLicenseNo(request.getLicenseNo());
        organization.setLegalRepresentative(request.getLegalRepresentative());
        organization.setSupervisorDepartment(request.getSupervisorDepartment());
        organization.setOrganizationLevel(request.getOrganizationLevel());
        organization.setOperationType(request.getOperationType());
        organization.setOrgGroupId(request.getOrgGroupId());
        organization.setParentId(request.getParentId());
        if (StringUtils.hasText(request.getOrgType())) {
            organization.setOrgType(Organization.OrgType.valueOf(request.getOrgType()));
        }
        organization.setCreatedBy(user);

        Organization saved = organizationRepository.save(organization);
        log.info("用户 {} 创建托育机构: {}", username, saved.getName());
        return convertToResponse(saved);
    }

    public List<OrganizationResponse> getMyOrganizations(String username) {
        User user = getUser(username);
        return organizationRepository.findByCreatedByOrderByCreatedAtDesc(user).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public OrganizationResponse getOrganizationDetail(String username, Long organizationId) {
        User user = getUser(username);
        Organization organization = getOwnedOrganization(user, organizationId);
        return convertToResponse(organization);
    }

    @Transactional
    public OrganizationResponse updateOrganization(String username, Long organizationId, OrganizationUpdateRequest request) {
        User user = getUser(username);
        Organization organization = getOwnedOrganization(user, organizationId);

        if (StringUtils.hasText(request.getName())) {
            organization.setName(request.getName());
        }
        if (request.getDescription() != null) {
            organization.setDescription(request.getDescription());
        }
        if (request.getContactPhone() != null) {
            organization.setContactPhone(request.getContactPhone());
        }
        if (request.getAddress() != null) {
            organization.setAddress(request.getAddress());
        }
        if (request.getRegistrationNo() != null) {
            organization.setRegistrationNo(request.getRegistrationNo());
        }
        if (request.getLicenseNo() != null) {
            organization.setLicenseNo(request.getLicenseNo());
        }
        if (request.getLegalRepresentative() != null) {
            organization.setLegalRepresentative(request.getLegalRepresentative());
        }
        if (request.getSupervisorDepartment() != null) {
            organization.setSupervisorDepartment(request.getSupervisorDepartment());
        }
        if (request.getOrganizationLevel() != null) {
            organization.setOrganizationLevel(request.getOrganizationLevel());
        }
        if (request.getOperationType() != null) {
            organization.setOperationType(request.getOperationType());
        }
        if (request.getOrgGroupId() != null) {
            organization.setOrgGroupId(request.getOrgGroupId());
        }
        if (request.getParentId() != null) {
            organization.setParentId(request.getParentId());
        }
        if (StringUtils.hasText(request.getOrgType())) {
            organization.setOrgType(Organization.OrgType.valueOf(request.getOrgType()));
        }
        if (StringUtils.hasText(request.getStatus())) {
            organization.setStatus(parseStatus(request.getStatus()));
        }
        if (request.getDailyReportApprovalRequired() != null) {
            organization.setDailyReportApprovalRequired(request.getDailyReportApprovalRequired());
        }

        Organization saved = organizationRepository.save(organization);
        log.info("用户 {} 更新托育机构: {}", username, saved.getName());
        return convertToResponse(saved);
    }

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

    /**
     * 任命园区负责人（园长）
     */
    @Transactional
    public void appointDirector(String username, Long organizationId, Long userId) {
        User operator = getUser(username);
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new BusinessException("机构不存在"));

        // 只有创建者可以任命园长
        if (!organizationRepository.existsByIdAndCreatedBy(organizationId, operator)) {
            throw new BusinessException("只有机构创建者可以任命园长");
        }

        User director = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 检查是否已有园长记录
        List<Staff> existingDirectors = staffRepository.findByOrganizationAndRoleAndStatus(
                organization, Staff.StaffRole.DIRECTOR, Staff.StaffStatus.ACTIVE);
        for (Staff sd : existingDirectors) {
            sd.setStatus(Staff.StaffStatus.DISABLED);
            staffRepository.save(sd);
        }

        // 创建或更新员工记录为园长
        if (staffRepository.existsByOrganizationAndUser(organization, director)) {
            // 查找已有员工记录更新角色
            List<Staff> existingStaff = staffRepository.findByOrganizationOrderByCreatedAtDesc(organization)
                    .stream().filter(s -> s.getUser().getId().equals(director.getId()))
                    .toList();
            for (Staff s : existingStaff) {
                s.setRole(Staff.StaffRole.DIRECTOR);
                s.setStatus(Staff.StaffStatus.ACTIVE);
                staffRepository.save(s);
            }
        } else {
            Staff newStaff = new Staff();
            newStaff.setOrganization(organization);
            newStaff.setUser(director);
            newStaff.setRole(Staff.StaffRole.DIRECTOR);
            newStaff.setStatus(Staff.StaffStatus.ACTIVE);
            staffRepository.save(newStaff);
        }

        log.info("用户 {} 任命 {} 为机构 {} 的园长", operator.getUsername(), director.getUsername(), organization.getName());
    }

    private Organization.OrganizationStatus parseStatus(String status) {
        try {
            return Organization.OrganizationStatus.valueOf(status);
        } catch (IllegalArgumentException error) {
            throw new BusinessException("机构状态不正确");
        }
    }

    private OrganizationResponse convertToResponse(Organization organization) {
        OrganizationResponse response = new OrganizationResponse();
        response.setId(organization.getId());
        response.setName(organization.getName());
        response.setDescription(organization.getDescription());
        response.setContactPhone(organization.getContactPhone());
        response.setAddress(organization.getAddress());
        response.setRegistrationNo(organization.getRegistrationNo());
        response.setLicenseNo(organization.getLicenseNo());
        response.setLegalRepresentative(organization.getLegalRepresentative());
        response.setSupervisorDepartment(organization.getSupervisorDepartment());
        response.setOrganizationLevel(organization.getOrganizationLevel());
        response.setOperationType(organization.getOperationType());
        response.setOrgGroupId(organization.getOrgGroupId());
        response.setParentId(organization.getParentId());
        response.setOrgType(organization.getOrgType());
        if (organization.getOrgType() != null) {
            response.setOrgTypeDescription(organization.getOrgType().getDescription());
        }
        // 集团名称
        if (organization.getOrgGroupId() != null) {
            orgGroupRepository.findById(organization.getOrgGroupId())
                .ifPresent(g -> response.setOrgGroupName(g.getName()));
        }
        // 上级机构名称
        if (organization.getParentId() != null) {
            organizationRepository.findById(organization.getParentId())
                .ifPresent(p -> response.setParentName(p.getName()));
        }
        response.setDailyReportApprovalRequired(organization.getDailyReportApprovalRequired());
        response.setStatus(organization.getStatus());
        response.setStatusDescription(organization.getStatus().getDescription());
        response.setCreatedBy(organization.getCreatedBy().getId());
        response.setCreatedAt(organization.getCreatedAt());
        response.setUpdatedAt(organization.getUpdatedAt());
        return response;
    }
}
