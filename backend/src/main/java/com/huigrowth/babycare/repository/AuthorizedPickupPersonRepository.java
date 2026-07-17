package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.AuthorizedPickupPerson;
import com.huigrowth.babycare.entity.Baby;
import com.huigrowth.babycare.entity.Classroom;
import com.huigrowth.babycare.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorizedPickupPersonRepository extends JpaRepository<AuthorizedPickupPerson, Long> {

    List<AuthorizedPickupPerson> findByEnrollmentOrderByCreatedAtDesc(Enrollment enrollment);

    List<AuthorizedPickupPerson> findByEnrollmentBabyOrderByCreatedAtDesc(Baby baby);

    List<AuthorizedPickupPerson> findByEnrollmentClassroomOrderByCreatedAtDesc(Classroom classroom);
}
