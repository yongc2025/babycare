package com.huigrowth.babycare.service;

import com.huigrowth.babycare.dto.ClassroomCreateRequest;
import com.huigrowth.babycare.dto.ClassroomResponse;
import com.huigrowth.babycare.dto.ClassroomUpdateRequest;
import com.huigrowth.babycare.entity.Classroom;
import com.huigrowth.babycare.entity.Organization;
import com.huigrowth.babycare.entity.User;
import com.huigrowth.babycare.exception.BusinessException;
import com.huigrowth.babycare.repository.ClassroomRepository;
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
 * 托育班级服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    @Transactional
    public ClassroomResponse createClassroom(String username, ClassroomCreateRequest request) {
        User user = getUser(username);
        Organization organization = getOwnedOrganization(user, request.getOrganizationId());
        validateAgeRange(request.getAgeRangeMinMonths(), request.getAgeRangeMaxMonths());

        Classroom classroom = new Classroom();
        classroom.setOrganization(organization);
        classroom.setName(request.getName());
        classroom.setAgeRangeMinMonths(request.getAgeRangeMinMonths());
        classroom.setAgeRangeMaxMonths(request.getAgeRangeMaxMonths());
        classroom.setCapacity(request.getCapacity());

        Classroom saved = classroomRepository.save(classroom);
        log.info("用户 {} 在机构 {} 创建班级: {}", username, organization.getName(), saved.getName());
        return convertToResponse(saved);
    }

    public List<ClassroomResponse> getOrganizationClassrooms(String username, Long organizationId) {
        User user = getUser(username);
        Organization organization = getOwnedOrganization(user, organizationId);
        return classroomRepository.findByOrganizationOrderByCreatedAtDesc(organization).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public ClassroomResponse getClassroomDetail(String username, Long classroomId) {
        User user = getUser(username);
        Classroom classroom = getOwnedClassroom(user, classroomId);
        return convertToResponse(classroom);
    }

    @Transactional
    public ClassroomResponse updateClassroom(String username, Long classroomId, ClassroomUpdateRequest request) {
        User user = getUser(username);
        Classroom classroom = getOwnedClassroom(user, classroomId);

        Integer nextMinAge = request.getAgeRangeMinMonths() != null
                ? request.getAgeRangeMinMonths()
                : classroom.getAgeRangeMinMonths();
        Integer nextMaxAge = request.getAgeRangeMaxMonths() != null
                ? request.getAgeRangeMaxMonths()
                : classroom.getAgeRangeMaxMonths();
        validateAgeRange(nextMinAge, nextMaxAge);

        if (StringUtils.hasText(request.getName())) {
            classroom.setName(request.getName());
        }
        if (request.getAgeRangeMinMonths() != null) {
            classroom.setAgeRangeMinMonths(request.getAgeRangeMinMonths());
        }
        if (request.getAgeRangeMaxMonths() != null) {
            classroom.setAgeRangeMaxMonths(request.getAgeRangeMaxMonths());
        }
        if (request.getCapacity() != null) {
            classroom.setCapacity(request.getCapacity());
        }
        if (StringUtils.hasText(request.getStatus())) {
            classroom.setStatus(parseStatus(request.getStatus()));
        }

        Classroom saved = classroomRepository.save(classroom);
        log.info("用户 {} 更新班级: {}", username, saved.getName());
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

    private Classroom getOwnedClassroom(User user, Long classroomId) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new BusinessException("班级不存在"));

        Long organizationId = classroom.getOrganization().getId();
        if (!organizationRepository.existsByIdAndCreatedBy(organizationId, user)) {
            throw new BusinessException("您无权访问该班级");
        }

        return classroom;
    }

    private void validateAgeRange(Integer minMonths, Integer maxMonths) {
        if (minMonths != null && maxMonths != null && minMonths > maxMonths) {
            throw new BusinessException("最小月龄不能大于最大月龄");
        }
    }

    private Classroom.ClassroomStatus parseStatus(String status) {
        try {
            return Classroom.ClassroomStatus.valueOf(status);
        } catch (IllegalArgumentException error) {
            throw new BusinessException("班级状态不正确");
        }
    }

    private ClassroomResponse convertToResponse(Classroom classroom) {
        ClassroomResponse response = new ClassroomResponse();
        response.setId(classroom.getId());
        response.setOrganizationId(classroom.getOrganization().getId());
        response.setOrganizationName(classroom.getOrganization().getName());
        response.setName(classroom.getName());
        response.setAgeRangeMinMonths(classroom.getAgeRangeMinMonths());
        response.setAgeRangeMaxMonths(classroom.getAgeRangeMaxMonths());
        response.setCapacity(classroom.getCapacity());
        response.setStatus(classroom.getStatus());
        response.setStatusDescription(classroom.getStatus().getDescription());
        response.setCreatedAt(classroom.getCreatedAt());
        response.setUpdatedAt(classroom.getUpdatedAt());
        return response;
    }
}
