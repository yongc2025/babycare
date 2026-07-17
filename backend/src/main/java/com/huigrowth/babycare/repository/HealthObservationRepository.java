package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.Baby;
import com.huigrowth.babycare.entity.Classroom;
import com.huigrowth.babycare.entity.Enrollment;
import com.huigrowth.babycare.entity.HealthObservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HealthObservationRepository extends JpaRepository<HealthObservation, Long> {

    List<HealthObservation> findByEnrollmentAndObservationDateOrderByObservationTimeDesc(
            Enrollment enrollment,
            LocalDate observationDate);

    List<HealthObservation> findByEnrollmentBabyAndObservationDateOrderByObservationTimeDesc(
            Baby baby,
            LocalDate observationDate);

    List<HealthObservation> findByEnrollmentClassroomAndObservationDateOrderByObservationTimeDesc(
            Classroom classroom,
            LocalDate observationDate);
}
