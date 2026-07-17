package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.Organization;
import com.huigrowth.babycare.entity.Staff;
import com.huigrowth.babycare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 机构员工数据访问层
 */
@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {

    List<Staff> findByOrganizationOrderByCreatedAtDesc(Organization organization);

    List<Staff> findByUserOrderByCreatedAtDesc(User user);

    boolean existsByOrganizationAndUser(Organization organization, User user);
}
