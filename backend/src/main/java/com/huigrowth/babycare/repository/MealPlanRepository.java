package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.MealPlan;
import com.huigrowth.babycare.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {

    List<MealPlan> findByOrganizationAndMealDateOrderByMealTypeAsc(
            Organization organization,
            LocalDate mealDate);

    List<MealPlan> findByOrganizationAndMealDateBetweenOrderByMealDateAscMealTypeAsc(
            Organization organization,
            LocalDate startDate,
            LocalDate endDate);
}
