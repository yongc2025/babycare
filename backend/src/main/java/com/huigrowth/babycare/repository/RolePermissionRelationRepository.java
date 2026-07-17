package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.RolePermissionRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRelationRepository extends JpaRepository<RolePermissionRelation, Long> {

    List<RolePermissionRelation> findByRoleId(Long roleId);

    void deleteByRoleId(Long roleId);
}
