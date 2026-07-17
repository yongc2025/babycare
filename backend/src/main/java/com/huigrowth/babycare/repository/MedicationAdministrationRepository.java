package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.MedicationAdministration;
import com.huigrowth.babycare.entity.MedicationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicationAdministrationRepository extends JpaRepository<MedicationAdministration, Long> {

    List<MedicationAdministration> findByMedicationRequestOrderByAdministeredAtDesc(
            MedicationRequest medicationRequest);
}
