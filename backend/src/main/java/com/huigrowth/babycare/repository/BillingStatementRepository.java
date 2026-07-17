package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.Baby;
import com.huigrowth.babycare.entity.BillingStatement;
import com.huigrowth.babycare.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillingStatementRepository extends JpaRepository<BillingStatement, Long> {

    List<BillingStatement> findByOrganizationOrderByCreatedAtDesc(Organization organization);

    List<BillingStatement> findByEnrollmentBabyOrderByCreatedAtDesc(Baby baby);
}
