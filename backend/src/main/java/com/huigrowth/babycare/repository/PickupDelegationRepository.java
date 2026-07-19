package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.Baby;
import com.huigrowth.babycare.entity.Classroom;
import com.huigrowth.babycare.entity.PickupDelegation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.huigrowth.babycare.entity.User;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PickupDelegationRepository extends JpaRepository<PickupDelegation, Long> {

    List<PickupDelegation> findByEnrollmentBabyOrderByPickupDateDescCreatedAtDesc(Baby baby);

    List<PickupDelegation> findByEnrollmentClassroomAndPickupDateOrderByCreatedAtDesc(
            Classroom classroom,
            LocalDate pickupDate);

    List<PickupDelegation> findByRequestedByOrderByCreatedAtDesc(User requestedBy);
}
