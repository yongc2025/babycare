package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.HardwareDevice;
import com.huigrowth.babycare.entity.HardwareEvent;
import com.huigrowth.babycare.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HardwareEventRepository extends JpaRepository<HardwareEvent, Long> {

    List<HardwareEvent> findByDeviceOrderByEventTimeDescCreatedAtDesc(HardwareDevice device);

    List<HardwareEvent> findByOrganizationOrderByEventTimeDescCreatedAtDesc(Organization organization);

    List<HardwareEvent> findByOrganizationAndEventTimeBetweenOrderByEventTimeDescCreatedAtDesc(
            Organization organization,
            LocalDateTime startTime,
            LocalDateTime endTime);
}
