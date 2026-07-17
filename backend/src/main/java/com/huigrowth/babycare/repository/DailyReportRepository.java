package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.Baby;
import com.huigrowth.babycare.entity.Classroom;
import com.huigrowth.babycare.entity.DailyReport;
import com.huigrowth.babycare.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyReportRepository extends JpaRepository<DailyReport, Long> {

    Optional<DailyReport> findByEnrollmentAndReportDate(Enrollment enrollment, LocalDate reportDate);

    Optional<DailyReport> findByEnrollmentBabyAndReportDate(Baby baby, LocalDate reportDate);

    List<DailyReport> findByEnrollmentClassroomAndReportDateOrderByCreatedAtDesc(
            Classroom classroom,
            LocalDate reportDate);

    List<DailyReport> findByEnrollmentBabyOrderByReportDateDesc(Baby baby);
}
