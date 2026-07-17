package com.huigrowth.babycare.service;

import com.huigrowth.babycare.dto.AnnouncementCreateRequest;
import com.huigrowth.babycare.dto.AnnouncementResponse;
import com.huigrowth.babycare.dto.AnnouncementUpdateRequest;
import com.huigrowth.babycare.entity.Announcement;
import com.huigrowth.babycare.entity.AnnouncementReceipt;
import com.huigrowth.babycare.entity.Classroom;
import com.huigrowth.babycare.entity.Organization;
import com.huigrowth.babycare.entity.User;
import com.huigrowth.babycare.exception.BusinessException;
import com.huigrowth.babycare.repository.AnnouncementReceiptRepository;
import com.huigrowth.babycare.repository.AnnouncementRepository;
import com.huigrowth.babycare.repository.ClassroomRepository;
import com.huigrowth.babycare.repository.OrganizationRepository;
import com.huigrowth.babycare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final AnnouncementReceiptRepository receiptRepository;
    private final OrganizationRepository organizationRepository;
    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;

    @Transactional
    public AnnouncementResponse createAnnouncement(String username, AnnouncementCreateRequest request) {
        User operator = getUser(username);
        Organization organization = getOwnedOrganization(operator, request.getOrganizationId());
        Classroom classroom = null;
        Announcement.AnnouncementScope scope = parseScope(request.getScope());
        if (scope == Announcement.AnnouncementScope.CLASSROOM) {
            classroom = classroomRepository.findById(request.getClassroomId())
                    .orElseThrow(() -> new BusinessException("班级不存在"));
            if (!classroom.getOrganization().getId().equals(organization.getId())) {
                throw new BusinessException("班级不属于该机构");
            }
        }

        Announcement announcement = new Announcement();
        announcement.setOrganization(organization);
        announcement.setClassroom(classroom);
        announcement.setScope(scope);
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setRequireReceipt(request.getRequireReceipt() != null ? request.getRequireReceipt() : true);

        return convert(announcementRepository.save(announcement), operator);
    }

    @Transactional
    public AnnouncementResponse updateAnnouncement(
            String username,
            Long announcementId,
            AnnouncementUpdateRequest request) {
        User operator = getUser(username);
        Announcement announcement = getOwnedAnnouncement(operator, announcementId);
        if (announcement.getStatus() == Announcement.AnnouncementStatus.PUBLISHED) {
            throw new BusinessException("已发布通知不能修改");
        }
        if (request.getTitle() != null) {
            announcement.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            announcement.setContent(request.getContent());
        }
        if (request.getRequireReceipt() != null) {
            announcement.setRequireReceipt(request.getRequireReceipt());
        }
        return convert(announcementRepository.save(announcement), operator);
    }

    @Transactional
    public AnnouncementResponse publishAnnouncement(String username, Long announcementId) {
        User operator = getUser(username);
        Announcement announcement = getOwnedAnnouncement(operator, announcementId);
        announcement.setStatus(Announcement.AnnouncementStatus.PUBLISHED);
        announcement.setPublishedAt(LocalDateTime.now());
        announcement.setPublishedBy(operator);
        return convert(announcementRepository.save(announcement), operator);
    }

    @Transactional
    public AnnouncementResponse markRead(String username, Long announcementId) {
        User operator = getUser(username);
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new BusinessException("通知不存在"));
        AnnouncementReceipt receipt = receiptRepository.findByAnnouncementAndUser(announcement, operator)
                .orElseGet(() -> {
                    AnnouncementReceipt next = new AnnouncementReceipt();
                    next.setAnnouncement(announcement);
                    next.setUser(operator);
                    return next;
                });
        receipt.setReadAt(LocalDateTime.now());
        receiptRepository.save(receipt);
        return convert(announcement, operator);
    }

    public List<AnnouncementResponse> getOrganizationAnnouncements(String username, Long organizationId) {
        User operator = getUser(username);
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new BusinessException("机构不存在"));
        if (!canAccessOrganization(operator, organization.getId())) {
            throw new BusinessException("您无权访问该机构通知");
        }
        return announcementRepository.findByOrganizationOrderByCreatedAtDesc(organization).stream()
                .map(announcement -> convert(announcement, operator))
                .collect(Collectors.toList());
    }

    public List<AnnouncementResponse> getClassroomAnnouncements(String username, Long classroomId) {
        User operator = getUser(username);
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new BusinessException("班级不存在"));
        if (!canAccessOrganization(operator, classroom.getOrganization().getId())) {
            throw new BusinessException("您无权访问该班级通知");
        }
        return announcementRepository.findByClassroomOrderByCreatedAtDesc(classroom).stream()
                .map(announcement -> convert(announcement, operator))
                .collect(Collectors.toList());
    }

    private Announcement getOwnedAnnouncement(User user, Long announcementId) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new BusinessException("通知不存在"));
        if (!canAccessOrganization(user, announcement.getOrganization().getId())) {
            throw new BusinessException("您无权操作该通知");
        }
        return announcement;
    }

    private Organization getOwnedOrganization(User user, Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new BusinessException("机构不存在"));
        if (!canAccessOrganization(user, organizationId)) {
            throw new BusinessException("您无权访问该机构");
        }
        return organization;
    }

    private boolean canAccessOrganization(User user, Long organizationId) {
        return organizationRepository.existsByIdAndCreatedBy(organizationId, user);
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    private Announcement.AnnouncementScope parseScope(String scope) {
        if (scope == null) {
            return Announcement.AnnouncementScope.ORGANIZATION;
        }
        try {
            return Announcement.AnnouncementScope.valueOf(scope);
        } catch (IllegalArgumentException error) {
            throw new BusinessException("通知范围不正确");
        }
    }

    private AnnouncementResponse convert(Announcement announcement, User currentUser) {
        AnnouncementResponse response = new AnnouncementResponse();
        response.setId(announcement.getId());
        response.setOrganizationId(announcement.getOrganization().getId());
        response.setOrganizationName(announcement.getOrganization().getName());
        if (announcement.getClassroom() != null) {
            response.setClassroomId(announcement.getClassroom().getId());
            response.setClassroomName(announcement.getClassroom().getName());
        }
        response.setScope(announcement.getScope());
        response.setScopeDescription(announcement.getScope().getDescription());
        response.setStatus(announcement.getStatus());
        response.setStatusDescription(announcement.getStatus().getDescription());
        response.setTitle(announcement.getTitle());
        response.setContent(announcement.getContent());
        response.setRequireReceipt(announcement.getRequireReceipt());
        response.setPublishedAt(announcement.getPublishedAt());
        if (announcement.getPublishedBy() != null) {
            response.setPublishedById(announcement.getPublishedBy().getId());
            response.setPublishedByName(announcement.getPublishedBy().getNickname());
        }
        receiptRepository.findByAnnouncementAndUser(announcement, currentUser).ifPresent(receipt -> {
            response.setReadByCurrentUser(true);
            response.setReadAt(receipt.getReadAt());
        });
        if (response.getReadByCurrentUser() == null) {
            response.setReadByCurrentUser(false);
        }
        response.setReceiptCount(receiptRepository.countByAnnouncement(announcement));
        response.setCreatedAt(announcement.getCreatedAt());
        response.setUpdatedAt(announcement.getUpdatedAt());
        return response;
    }
}
