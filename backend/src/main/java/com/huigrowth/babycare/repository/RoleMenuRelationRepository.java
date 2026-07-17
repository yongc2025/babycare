package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.RoleMenuRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleMenuRelationRepository extends JpaRepository<RoleMenuRelation, Long> {

    List<RoleMenuRelation> findByRoleId(Long roleId);

    void deleteByRoleId(Long roleId);
}
