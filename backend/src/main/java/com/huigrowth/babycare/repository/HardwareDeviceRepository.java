package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.HardwareDevice;
import com.huigrowth.babycare.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HardwareDeviceRepository extends JpaRepository<HardwareDevice, Long> {

    List<HardwareDevice> findByOrganizationOrderByCreatedAtDesc(Organization organization);

    List<HardwareDevice> findByOrganizationAndDeviceTypeOrderByCreatedAtDesc(
            Organization organization,
            HardwareDevice.DeviceType deviceType);

    List<HardwareDevice> findByOrganizationAndStatusOrderByCreatedAtDesc(
            Organization organization,
            HardwareDevice.DeviceStatus status);

    Optional<HardwareDevice> findByOrganizationAndDeviceCode(Organization organization, String deviceCode);

    boolean existsByOrganizationAndDeviceCode(Organization organization, String deviceCode);
}
