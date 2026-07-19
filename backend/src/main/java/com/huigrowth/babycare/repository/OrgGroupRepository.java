package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.OrgGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrgGroupRepository extends JpaRepository<OrgGroup, Long> {

    Optional<OrgGroup> findByCode(String code);

    boolean existsByCode(String code);
}
