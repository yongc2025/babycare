package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.Enrollment;
import com.huigrowth.babycare.entity.EnrollmentGuardian;
import com.huigrowth.babycare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 入托监护人关系仓库
 */
@Repository
public interface EnrollmentGuardianRepository extends JpaRepository<EnrollmentGuardian, Long> {

    List<EnrollmentGuardian> findByEnrollmentOrderByCreatedAtAsc(Enrollment enrollment);

    List<EnrollmentGuardian> findByGuardianUserOrderByCreatedAtDesc(User guardianUser);

    Optional<EnrollmentGuardian> findByEnrollmentAndGuardianUser(Enrollment enrollment, User guardianUser);

    boolean existsByEnrollmentAndGuardianUser(Enrollment enrollment, User guardianUser);

    Optional<EnrollmentGuardian> findByInviteCode(String inviteCode);

    long countByEnrollment(Enrollment enrollment);
}
