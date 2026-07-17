package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.AttendanceRecord;
import com.huigrowth.babycare.entity.Baby;
import com.huigrowth.babycare.entity.Classroom;
import com.huigrowth.babycare.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 幼儿考勤记录数据访问层
 */
@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    Optional<AttendanceRecord> findByEnrollmentAndAttendanceDate(Enrollment enrollment, LocalDate attendanceDate);

    List<AttendanceRecord> findByEnrollmentClassroomAndAttendanceDateOrderByCreatedAtDesc(
            Classroom classroom,
            LocalDate attendanceDate);

    List<AttendanceRecord> findByEnrollmentBabyAndAttendanceDateBetweenOrderByAttendanceDateDesc(
            Baby baby,
            LocalDate startDate,
            LocalDate endDate);
}
