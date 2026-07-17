package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.Baby;
import com.huigrowth.babycare.entity.CareRecord;
import com.huigrowth.babycare.entity.Classroom;
import com.huigrowth.babycare.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CareRecordRepository extends JpaRepository<CareRecord, Long> {

    List<CareRecord> findByEnrollmentAndRecordDateOrderByRecordTimeDesc(
            Enrollment enrollment,
            LocalDate recordDate);

    List<CareRecord> findByEnrollmentBabyAndRecordDateOrderByRecordTimeDesc(
            Baby baby,
            LocalDate recordDate);

    List<CareRecord> findByEnrollmentClassroomAndRecordDateOrderByRecordTimeDesc(
            Classroom classroom,
            LocalDate recordDate);
}
