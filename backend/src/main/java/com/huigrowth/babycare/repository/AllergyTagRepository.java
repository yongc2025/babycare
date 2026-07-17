package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.AllergyTag;
import com.huigrowth.babycare.entity.Baby;
import com.huigrowth.babycare.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllergyTagRepository extends JpaRepository<AllergyTag, Long> {

    List<AllergyTag> findByEnrollmentOrderByCreatedAtDesc(Enrollment enrollment);

    List<AllergyTag> findByEnrollmentBabyOrderByCreatedAtDesc(Baby baby);
}
