package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    List<Menu> findByParentIdOrderBySortOrderAsc(Long parentId);

    List<Menu> findByStatusOrderBySortOrderAsc(Menu.MenuStatus status);

    @Query("SELECT m FROM Menu m JOIN RoleMenuRelation rm ON m.id = rm.menuId WHERE rm.roleId = ?1 ORDER BY m.sortOrder ASC")
    List<Menu> findByRoleId(Long roleId);

    @Query("SELECT DISTINCT m FROM Menu m JOIN RoleMenuRelation rm ON m.id = rm.menuId " +
           "JOIN UserRoleRelation ur ON rm.roleId = ur.roleId WHERE ur.userId = ?1 ORDER BY m.sortOrder ASC")
    List<Menu> findByUserId(Long userId);
}
