package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.Baby;
import com.huigrowth.babycare.entity.Classroom;
import com.huigrowth.babycare.entity.IncidentReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentReportRepository extends JpaRepository<IncidentReport, Long> {

    List<IncidentReport> findByEnrollmentBabyOrderByOccurredAtDesc(Baby baby);

    List<IncidentReport> findByEnrollmentClassroomOrderByOccurredAtDesc(Classroom classroom);

    List<IncidentReport> findByEnrollmentClassroomAndStatusOrderByOccurredAtDesc(
            Classroom classroom,
            IncidentReport.IncidentStatus status);
}
