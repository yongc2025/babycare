package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.SafetyLedger;
import com.huigrowth.babycare.entity.SafetyLedgerTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SafetyLedgerTemplateRepository extends JpaRepository<SafetyLedgerTemplate, Long> {

    List<SafetyLedgerTemplate> findByOrganizationId(Long organizationId);

    List<SafetyLedgerTemplate> findByOrganizationIdAndIsActiveTrue(Long organizationId);

    @Query("SELECT t FROM SafetyLedgerTemplate t WHERE t.isActive = true AND t.nextGenerateDate <= :date")
    List<SafetyLedgerTemplate> findActiveTemplatesDueOnOrBefore(LocalDate date);

    long countByOrganizationIdAndIsActiveTrue(Long organizationId);
}
