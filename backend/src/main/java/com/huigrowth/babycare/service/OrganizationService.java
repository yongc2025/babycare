package com.huigrowth.babycare.service;

import com.huigrowth.babycare.dto.OrganizationCreateRequest;
import com.huigrowth.babycare.dto.OrganizationResponse;
import com.huigrowth.babycare.dto.OrganizationUpdateRequest;
import com.huigrowth.babycare.entity.Organization;
import com.huigrowth.babycare.entity.User;
import com.huigrowth.babycare.exception.BusinessException;
import com.huigrowth.babycare.repository.OrganizationRepository;
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
        if (StringUtils.hasText(request.getStatus())) {
            organization.setStatus(parseStatus(request.getStatus()));
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
        response.setStatus(organization.getStatus());
        response.setStatusDescription(organization.getStatus().getDescription());
        response.setCreatedBy(organization.getCreatedBy().getId());
        response.setCreatedAt(organization.getCreatedAt());
        response.setUpdatedAt(organization.getUpdatedAt());
        return response;
    }
}
