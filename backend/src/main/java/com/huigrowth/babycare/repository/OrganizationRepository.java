package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.Organization;
import com.huigrowth.babycare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 托育机构数据访问层
 */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    List<Organization> findByCreatedByOrderByCreatedAtDesc(User createdBy);

    boolean existsByIdAndCreatedBy(Long id, User createdBy);
}
