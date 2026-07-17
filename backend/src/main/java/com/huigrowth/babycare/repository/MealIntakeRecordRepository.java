package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.Enrollment;
import com.huigrowth.babycare.entity.MealIntakeRecord;
import com.huigrowth.babycare.entity.MealPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MealIntakeRecordRepository extends JpaRepository<MealIntakeRecord, Long> {

    Optional<MealIntakeRecord> findByMealPlanAndEnrollment(MealPlan mealPlan, Enrollment enrollment);

    List<MealIntakeRecord> findByMealPlanOrderByCreatedAtDesc(MealPlan mealPlan);

    List<MealIntakeRecord> findByEnrollmentOrderByCreatedAtDesc(Enrollment enrollment);
}
