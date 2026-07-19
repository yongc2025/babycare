package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.FollowUpRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FollowUpRecordRepository extends JpaRepository<FollowUpRecord, Long> {

    List<FollowUpRecord> findByAdmissionLeadIdOrderByCreatedAtDesc(Long admissionLeadId);

    long countByNextFollowUpAtBeforeAndNextFollowUpAtIsNotNull(LocalDateTime dateTime);
}
