package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.Baby;
import com.huigrowth.babycare.entity.Classroom;
import com.huigrowth.babycare.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 幼儿请假申请数据访问层
 */
@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEnrollmentClassroomOrderByCreatedAtDesc(Classroom classroom);

    List<LeaveRequest> findByEnrollmentBabyOrderByCreatedAtDesc(Baby baby);
}
