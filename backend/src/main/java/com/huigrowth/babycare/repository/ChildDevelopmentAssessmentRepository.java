package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.Baby;
import com.huigrowth.babycare.entity.ChildDevelopmentAssessment;
import com.huigrowth.babycare.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChildDevelopmentAssessmentRepository
        extends JpaRepository<ChildDevelopmentAssessment, Long> {

    List<ChildDevelopmentAssessment> findByEnrollmentOrderByAssessmentDateDescCreatedAtDesc(
            Enrollment enrollment);

    List<ChildDevelopmentAssessment> findByEnrollmentBabyOrderByAssessmentDateDescCreatedAtDesc(
            Baby baby);

    List<ChildDevelopmentAssessment> findByEnrollmentAndAssessmentModeOrderByAssessmentDateDescCreatedAtDesc(
            Enrollment enrollment,
            ChildDevelopmentAssessment.AssessmentMode assessmentMode);
}
