package com.huigrowth.babycare.service;

import com.huigrowth.babycare.dto.HardwareDeviceRequest;
import com.huigrowth.babycare.dto.HardwareDeviceResponse;
import com.huigrowth.babycare.dto.HardwareEventIngestRequest;
import com.huigrowth.babycare.dto.HardwareEventResponse;
import com.huigrowth.babycare.dto.HardwareEventStatusRequest;
import com.huigrowth.babycare.entity.Classroom;
import com.huigrowth.babycare.entity.Enrollment;
import com.huigrowth.babycare.entity.HardwareDevice;
import com.huigrowth.babycare.entity.HardwareEvent;
import com.huigrowth.babycare.entity.Organization;
import com.huigrowth.babycare.entity.User;
import com.huigrowth.babycare.exception.BusinessException;
import com.huigrowth.babycare.repository.ClassroomRepository;
import com.huigrowth.babycare.repository.EnrollmentRepository;
import com.huigrowth.babycare.repository.HardwareDeviceRepository;
import com.huigrowth.babycare.repository.HardwareEventRepository;
import com.huigrowth.babycare.repository.OrganizationRepository;
import com.huigrowth.babycare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HardwareIntegrationService {

    private final HardwareDeviceRepository hardwareDeviceRepository;
    private final HardwareEventRepository hardwareEventRepository;
    private final OrganizationRepository organizationRepository;
    private final ClassroomRepository classroomRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    @Transactional
    public HardwareDeviceResponse createDevice(String username, HardwareDeviceRequest request) {
        User operator = getUser(username);
        Organization organization = getOwnedOrganization(operator, request.getOrganizationId());
        if (hardwareDeviceRepository.existsByOrganizationAndDeviceCode(organization, request.getDeviceCode())) {
            throw new BusinessException("同一机构下设备编码已存在");
        }

        HardwareDevice device = new HardwareDevice();
        device.setOrganization(organization);
        device.setClassroom(getClassroomInOrganization(request.getClassroomId(), organization));
        device.setDeviceCode(request.getDeviceCode());
        device.setName(request.getName());
        device.setDeviceType(parseDeviceType(request.getDeviceType()));
        device.setVendor(request.getVendor());
        device.setModel(request.getModel());
        device.setLocation(request.getLocation());
        if (StringUtils.hasText(request.getIntegrationMode())) {
            device.setIntegrationMode(request.getIntegrationMode());
        }
        if (StringUtils.hasText(request.getStatus())) {
            device.setStatus(parseDeviceStatus(request.getStatus()));
        }
        device.setRemark(request.getRemark());
        return convertDevice(hardwareDeviceRepository.save(device));
    }

    @Transactional
    public HardwareDeviceResponse updateDevice(String username, Long deviceId, HardwareDeviceRequest request) {
        User operator = getUser(username);
        HardwareDevice device = getOwnedDevice(operator, deviceId);
        Organization organization = device.getOrganization();

        if (StringUtils.hasText(request.getDeviceCode())
                && !request.getDeviceCode().equals(device.getDeviceCode())
                && hardwareDeviceRepository.existsByOrganizationAndDeviceCode(organization, request.getDeviceCode())) {
            throw new BusinessException("同一机构下设备编码已存在");
        }

        if (request.getClassroomId() != null) {
            device.setClassroom(getClassroomInOrganization(request.getClassroomId(), organization));
        }
        if (StringUtils.hasText(request.getDeviceCode())) {
            device.setDeviceCode(request.getDeviceCode());
        }
        if (StringUtils.hasText(request.getName())) {
            device.setName(request.getName());
        }
        if (StringUtils.hasText(request.getDeviceType())) {
            device.setDeviceType(parseDeviceType(request.getDeviceType()));
        }
        if (request.getVendor() != null) {
            device.setVendor(request.getVendor());
        }
        if (request.getModel() != null) {
            device.setModel(request.getModel());
        }
        if (request.getLocation() != null) {
            device.setLocation(request.getLocation());
        }
        if (request.getIntegrationMode() != null) {
            device.setIntegrationMode(request.getIntegrationMode());
        }
        if (StringUtils.hasText(request.getStatus())) {
            device.setStatus(parseDeviceStatus(request.getStatus()));
        }
        if (request.getRemark() != null) {
            device.setRemark(request.getRemark());
        }
        return convertDevice(hardwareDeviceRepository.save(device));
    }

    public HardwareDeviceResponse getDeviceDetail(String username, Long deviceId) {
        User operator = getUser(username);
        return convertDevice(getOwnedDevice(operator, deviceId));
    }

    public List<HardwareDeviceResponse> getOrganizationDevices(
            String username,
            Long organizationId,
            String deviceType,
            String status) {
        User operator = getUser(username);
        Organization organization = getOwnedOrganization(operator, organizationId);
        List<HardwareDevice> devices;
        if (StringUtils.hasText(deviceType)) {
            devices = hardwareDeviceRepository.findByOrganizationAndDeviceTypeOrderByCreatedAtDesc(
                    organization,
                    parseDeviceType(deviceType));
        } else if (StringUtils.hasText(status)) {
            devices = hardwareDeviceRepository.findByOrganizationAndStatusOrderByCreatedAtDesc(
                    organization,
                    parseDeviceStatus(status));
        } else {
            devices = hardwareDeviceRepository.findByOrganizationOrderByCreatedAtDesc(organization);
        }
        return devices.stream().map(this::convertDevice).collect(Collectors.toList());
    }

    @Transactional
    public HardwareEventResponse ingestEvent(String username, HardwareEventIngestRequest request) {
        User operator = getUser(username);
        HardwareDevice device = resolveDevice(operator, request);
        Organization organization = device.getOrganization();
        Classroom classroom = request.getClassroomId() != null
                ? getClassroomInOrganization(request.getClassroomId(), organization)
                : device.getClassroom();
        Enrollment enrollment = getEnrollmentInOrganization(request.getEnrollmentId(), organization);

        HardwareEvent event = new HardwareEvent();
        event.setDevice(device);
        event.setOrganization(organization);
        event.setClassroom(classroom);
        event.setEnrollment(enrollment);
        event.setEventType(parseEventType(request.getEventType()));
        event.setEventTime(request.getEventTime() != null ? request.getEventTime() : LocalDateTime.now());
        event.setEventKey(request.getEventKey());
        event.setSubjectRef(request.getSubjectRef());
        event.setConfidence(request.getConfidence());
        event.setPayload(request.getPayload());
        event.setStatus(HardwareEvent.EventStatus.RECEIVED);
        event.setProcessRemark(Boolean.TRUE.equals(request.getRawOnly())
                ? "raw event only; business mapping pending"
                : "received; waiting for business mapping");

        device.setLastSeenAt(event.getEventTime());
        hardwareDeviceRepository.save(device);
        return convertEvent(hardwareEventRepository.save(event));
    }

    @Transactional
    public HardwareEventResponse updateEventStatus(
            String username,
            Long eventId,
            HardwareEventStatusRequest request) {
        User operator = getUser(username);
        HardwareEvent event = hardwareEventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException("硬件事件不存在"));
        if (!organizationRepository.existsByIdAndCreatedBy(event.getOrganization().getId(), operator)) {
            throw new BusinessException("您无权处理该硬件事件");
        }

        HardwareEvent.EventStatus status = parseEventStatus(request.getStatus());
        event.setStatus(status);
        event.setProcessRemark(request.getProcessRemark());
        event.setProcessedAt(status == HardwareEvent.EventStatus.RECEIVED ? null : LocalDateTime.now());
        return convertEvent(hardwareEventRepository.save(event));
    }

    public List<HardwareEventResponse> getDeviceEvents(String username, Long deviceId) {
        User operator = getUser(username);
        HardwareDevice device = getOwnedDevice(operator, deviceId);
        return hardwareEventRepository.findByDeviceOrderByEventTimeDescCreatedAtDesc(device).stream()
                .map(this::convertEvent)
                .collect(Collectors.toList());
    }

    public List<HardwareEventResponse> getOrganizationEvents(
            String username,
            Long organizationId,
            LocalDate startDate,
            LocalDate endDate) {
        User operator = getUser(username);
        Organization organization = getOwnedOrganization(operator, organizationId);
        List<HardwareEvent> events;
        if (startDate != null || endDate != null) {
            LocalDate start = startDate != null ? startDate : LocalDate.now();
            LocalDate end = endDate != null ? endDate : start;
            if (start.isAfter(end)) {
                throw new BusinessException("开始日期不能晚于结束日期");
            }
            events = hardwareEventRepository.findByOrganizationAndEventTimeBetweenOrderByEventTimeDescCreatedAtDesc(
                    organization,
                    start.atStartOfDay(),
                    end.atTime(LocalTime.MAX));
        } else {
            events = hardwareEventRepository.findByOrganizationOrderByEventTimeDescCreatedAtDesc(organization);
        }
        return events.stream().map(this::convertEvent).collect(Collectors.toList());
    }

    private HardwareDevice resolveDevice(User operator, HardwareEventIngestRequest request) {
        if (request.getDeviceId() != null) {
            return getOwnedDevice(operator, request.getDeviceId());
        }
        if (request.getOrganizationId() == null || !StringUtils.hasText(request.getDeviceCode())) {
            throw new BusinessException("deviceId 或 organizationId + deviceCode 必须提供");
        }
        Organization organization = getOwnedOrganization(operator, request.getOrganizationId());
        return hardwareDeviceRepository.findByOrganizationAndDeviceCode(organization, request.getDeviceCode())
                .orElseThrow(() -> new BusinessException("硬件设备不存在"));
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

    private HardwareDevice getOwnedDevice(User user, Long deviceId) {
        HardwareDevice device = hardwareDeviceRepository.findById(deviceId)
                .orElseThrow(() -> new BusinessException("硬件设备不存在"));
        if (!organizationRepository.existsByIdAndCreatedBy(device.getOrganization().getId(), user)) {
            throw new BusinessException("您无权访问该硬件设备");
        }
        return device;
    }

    private Classroom getClassroomInOrganization(Long classroomId, Organization organization) {
        if (classroomId == null) return null;
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new BusinessException("班级不存在"));
        if (!classroom.getOrganization().getId().equals(organization.getId())) {
            throw new BusinessException("班级不属于该机构");
        }
        return classroom;
    }

    private Enrollment getEnrollmentInOrganization(Long enrollmentId, Organization organization) {
        if (enrollmentId == null) return null;
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new BusinessException("入托档案不存在"));
        if (!enrollment.getOrganization().getId().equals(organization.getId())) {
            throw new BusinessException("入托档案不属于该机构");
        }
        return enrollment;
    }

    private HardwareDevice.DeviceType parseDeviceType(String deviceType) {
        if (!StringUtils.hasText(deviceType)) return HardwareDevice.DeviceType.OTHER;
        try {
            return HardwareDevice.DeviceType.valueOf(deviceType);
        } catch (IllegalArgumentException error) {
            throw new BusinessException("硬件设备类型不正确");
        }
    }

    private HardwareDevice.DeviceStatus parseDeviceStatus(String status) {
        if (!StringUtils.hasText(status)) return HardwareDevice.DeviceStatus.ACTIVE;
        try {
            return HardwareDevice.DeviceStatus.valueOf(status);
        } catch (IllegalArgumentException error) {
            throw new BusinessException("硬件设备状态不正确");
        }
    }

    private HardwareEvent.EventType parseEventType(String eventType) {
        if (!StringUtils.hasText(eventType)) return HardwareEvent.EventType.RAW_MESSAGE;
        try {
            return HardwareEvent.EventType.valueOf(eventType);
        } catch (IllegalArgumentException error) {
            throw new BusinessException("硬件事件类型不正确");
        }
    }

    private HardwareEvent.EventStatus parseEventStatus(String status) {
        try {
            return HardwareEvent.EventStatus.valueOf(status);
        } catch (IllegalArgumentException error) {
            throw new BusinessException("硬件事件状态不正确");
        }
    }

    private HardwareDeviceResponse convertDevice(HardwareDevice device) {
        HardwareDeviceResponse response = new HardwareDeviceResponse();
        response.setId(device.getId());
        response.setOrganizationId(device.getOrganization().getId());
        response.setOrganizationName(device.getOrganization().getName());
        if (device.getClassroom() != null) {
            response.setClassroomId(device.getClassroom().getId());
            response.setClassroomName(device.getClassroom().getName());
        }
        response.setDeviceCode(device.getDeviceCode());
        response.setName(device.getName());
        response.setDeviceType(device.getDeviceType());
        response.setDeviceTypeDescription(device.getDeviceType().getDescription());
        response.setVendor(device.getVendor());
        response.setModel(device.getModel());
        response.setLocation(device.getLocation());
        response.setIntegrationMode(device.getIntegrationMode());
        response.setStatus(device.getStatus());
        response.setStatusDescription(device.getStatus().getDescription());
        response.setLastSeenAt(device.getLastSeenAt());
        response.setRemark(device.getRemark());
        response.setCreatedAt(device.getCreatedAt());
        response.setUpdatedAt(device.getUpdatedAt());
        return response;
    }

    private HardwareEventResponse convertEvent(HardwareEvent event) {
        HardwareEventResponse response = new HardwareEventResponse();
        response.setId(event.getId());
        response.setDeviceId(event.getDevice().getId());
        response.setDeviceCode(event.getDevice().getDeviceCode());
        response.setDeviceName(event.getDevice().getName());
        response.setOrganizationId(event.getOrganization().getId());
        response.setOrganizationName(event.getOrganization().getName());
        if (event.getClassroom() != null) {
            response.setClassroomId(event.getClassroom().getId());
            response.setClassroomName(event.getClassroom().getName());
        }
        if (event.getEnrollment() != null) {
            response.setEnrollmentId(event.getEnrollment().getId());
            response.setBabyName(event.getEnrollment().getBaby().getName());
        }
        response.setEventType(event.getEventType());
        response.setEventTypeDescription(event.getEventType().getDescription());
        response.setEventTime(event.getEventTime());
        response.setEventKey(event.getEventKey());
        response.setSubjectRef(event.getSubjectRef());
        response.setConfidence(event.getConfidence());
        response.setPayload(event.getPayload());
        response.setStatus(event.getStatus());
        response.setStatusDescription(event.getStatus().getDescription());
        response.setProcessedAt(event.getProcessedAt());
        response.setProcessRemark(event.getProcessRemark());
        response.setCreatedAt(event.getCreatedAt());
        response.setUpdatedAt(event.getUpdatedAt());
        return response;
    }
}
