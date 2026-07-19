package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.Baby;
import com.huigrowth.babycare.entity.Classroom;
import com.huigrowth.babycare.entity.MedicationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.huigrowth.babycare.entity.User;
import java.util.List;

@Repository
public interface MedicationRequestRepository extends JpaRepository<MedicationRequest, Long> {

    List<MedicationRequest> findByEnrollmentBabyOrderByCreatedAtDesc(Baby baby);

    List<MedicationRequest> findByEnrollmentClassroomOrderByCreatedAtDesc(Classroom classroom);

    List<MedicationRequest> findByRequestedByOrderByCreatedAtDesc(User requestedBy);
}
