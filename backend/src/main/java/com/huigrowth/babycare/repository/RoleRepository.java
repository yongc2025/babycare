package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByCode(String code);

    boolean existsByCode(String code);

    List<Role> findByStatus(Role.RoleStatus status);

    @Query("SELECT r FROM Role r JOIN UserRoleRelation ur ON r.id = ur.roleId WHERE ur.userId = ?1")
    List<Role> findByUserId(Long userId);
}
