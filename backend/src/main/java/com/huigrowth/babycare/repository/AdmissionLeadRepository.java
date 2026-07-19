package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.AdmissionLead;
import com.huigrowth.babycare.entity.AdmissionLead;
import com.huigrowth.babycare.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdmissionLeadRepository extends JpaRepository<AdmissionLead, Long> {

    List<AdmissionLead> findByOrganizationOrderByCreatedAtDesc(Organization organization);

    List<AdmissionLead> findByOrganizationAndStatusOrderByCreatedAtDesc(
            Organization organization,
            AdmissionLead.LeadStatus status);

    long countByOrganizationAndStatus(
            Organization organization,
            AdmissionLead.LeadStatus status);
}
