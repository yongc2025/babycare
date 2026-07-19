package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.Baby;
import com.huigrowth.babycare.entity.BillingStatement;
import com.huigrowth.babycare.entity.Enrollment;
import com.huigrowth.babycare.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BillingStatementRepository extends JpaRepository<BillingStatement, Long> {

    List<BillingStatement> findByOrganizationOrderByCreatedAtDesc(Organization organization);

    List<BillingStatement> findByEnrollmentBabyOrderByCreatedAtDesc(Baby baby);

    List<BillingStatement> findByEnrollmentInOrderByCreatedAtDesc(List<Enrollment> enrollments);

    long countByOrganizationAndStatus(
            Organization organization,
            BillingStatement.BillingStatus status);

    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM BillingStatement b WHERE b.organization = :organization AND b.status = :status")
    BigDecimal sumAmountByOrganizationAndStatus(
            Organization organization,
            BillingStatement.BillingStatus status);
}
