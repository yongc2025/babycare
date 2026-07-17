package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.Classroom;
import com.huigrowth.babycare.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 托育班级数据访问层
 */
@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

    List<Classroom> findByOrganizationOrderByCreatedAtDesc(Organization organization);
}
