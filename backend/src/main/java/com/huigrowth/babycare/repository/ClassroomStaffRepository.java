package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.ClassroomStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassroomStaffRepository extends JpaRepository<ClassroomStaff, Long> {

    List<ClassroomStaff> findByClassroomId(Long classroomId);

    List<ClassroomStaff> findByStaffId(Long staffId);

    boolean existsByClassroomIdAndStaffId(Long classroomId, Long staffId);
}
