package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.Organization;
import com.huigrowth.babycare.entity.SafetyLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SafetyLedgerRepository extends JpaRepository<SafetyLedger, Long> {

    List<SafetyLedger> findByOrganizationOrderByLedgerDateDescCreatedAtDesc(Organization organization);

    List<SafetyLedger> findByOrganizationAndLedgerDateBetweenOrderByLedgerDateDescCreatedAtDesc(
            Organization organization,
            LocalDate startDate,
            LocalDate endDate);

    List<SafetyLedger> findByOrganizationAndLedgerTypeOrderByLedgerDateDescCreatedAtDesc(
            Organization organization,
            SafetyLedger.LedgerType ledgerType);

    List<SafetyLedger> findByOrganizationAndStatusOrderByLedgerDateDescCreatedAtDesc(
            Organization organization,
            SafetyLedger.LedgerStatus status);
}
