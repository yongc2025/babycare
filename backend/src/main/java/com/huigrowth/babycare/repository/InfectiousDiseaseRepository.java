package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.InfectiousDisease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InfectiousDiseaseRepository extends JpaRepository<InfectiousDisease, Long> {

    List<InfectiousDisease> findByClassroomIdOrderByCreatedAtDesc(Long classroomId);

    List<InfectiousDisease> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId);

    List<InfectiousDisease> findByEnrollmentIdOrderByCreatedAtDesc(Long enrollmentId);

    long countByOrganizationIdAndStatusIn(Long organizationId, List<InfectiousDisease.DiseaseStatus> statuses);
}
