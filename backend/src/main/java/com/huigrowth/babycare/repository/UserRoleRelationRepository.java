package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.UserRoleRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRelationRepository extends JpaRepository<UserRoleRelation, Long> {

    List<UserRoleRelation> findByUserId(Long userId);

    List<UserRoleRelation> findByRoleId(Long roleId);

    boolean existsByUserIdAndRoleId(Long userId, Long roleId);

    void deleteByUserId(Long userId);

    void deleteByUserIdAndRoleId(Long userId, Long roleId);
}
