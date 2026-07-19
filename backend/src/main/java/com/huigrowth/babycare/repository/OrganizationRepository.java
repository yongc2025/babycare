package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.Organization;
import com.huigrowth.babycare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 托育机构数据访问层
 */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    List<Organization> findByCreatedByOrderByCreatedAtDesc(User createdBy);

    boolean existsByIdAndCreatedBy(Long id, User createdBy);

    @Query("SELECT o FROM Organization o JOIN Staff s ON o = s.organization WHERE s.user = ?1 AND s.role = 'DIRECTOR' AND s.status = 'ACTIVE'")
    List<Organization> findByDirector(User director);

    @Query("SELECT o FROM Organization o JOIN Staff s ON o = s.organization WHERE s.user = ?1 AND s.status = 'ACTIVE'")
    List<Organization> findByStaffUser(User user);
}
