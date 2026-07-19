package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.StaffClassroomAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 员工-班级关联数据访问层
 */
@Repository
public interface StaffClassroomAssignmentRepository extends JpaRepository<StaffClassroomAssignment, Long> {

    List<StaffClassroomAssignment> findByClassroomId(Long classroomId);

    List<StaffClassroomAssignment> findByStaffId(Long staffId);

    Optional<StaffClassroomAssignment> findByStaffIdAndClassroomId(Long staffId, Long classroomId);

    List<StaffClassroomAssignment> findByClassroomIdAndAssignmentType(Long classroomId, StaffClassroomAssignment.AssignmentType assignmentType);

    void deleteByStaffIdAndClassroomId(Long staffId, Long classroomId);

    boolean existsByStaffIdAndClassroomId(Long staffId, Long classroomId);
}
