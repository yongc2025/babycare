package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.EnrollmentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 入托档案状态变更历史仓库（T077）
 */
public interface EnrollmentStatusHistoryRepository extends JpaRepository<EnrollmentStatusHistory, Long> {

    List<EnrollmentStatusHistory> findByEnrollmentIdOrderByCreatedAtAsc(Long enrollmentId);
}
