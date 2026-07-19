package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.Baby;
import com.huigrowth.babycare.entity.Classroom;
import com.huigrowth.babycare.entity.Enrollment;
import com.huigrowth.babycare.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 宝宝入托档案数据访问层
 */
@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByClassroomOrderByCreatedAtDesc(Classroom classroom);

    List<Enrollment> findByOrganizationOrderByCreatedAtDesc(Organization organization);

    Optional<Enrollment> findByBabyAndOrganization(Baby baby, Organization organization);

    List<Enrollment> findByBabyOrderByCreatedAtDesc(Baby baby);

    boolean existsByBabyAndOrganizationAndStatusIn(
            Baby baby,
            Organization organization,
            Collection<Enrollment.EnrollmentStatus> statuses);

    List<Enrollment> findByOrganizationAndStatusOrderByCreatedAtDesc(Organization organization, Enrollment.EnrollmentStatus status);
}
