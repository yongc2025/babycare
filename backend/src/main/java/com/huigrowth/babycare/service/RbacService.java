package com.huigrowth.babycare.service;

import com.huigrowth.babycare.dto.*;
import com.huigrowth.babycare.entity.*;
import com.huigrowth.babycare.exception.BusinessException;
import com.huigrowth.babycare.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RBAC 权限管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RbacService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final MenuRepository menuRepository;
    private final UserRoleRelationRepository userRoleRelationRepository;
    private final RoleMenuRelationRepository roleMenuRelationRepository;
    private final RolePermissionRelationRepository rolePermissionRelationRepository;
    private final UserRepository userRepository;

    // ========== 角色管理 ==========

    public List<RoleResponse> listRoles() {
        return roleRepository.findAll().stream()
                .map(r -> {
                    RoleResponse resp = RoleResponse.fromEntity(r);
                    resp.setUserCount(userRoleRelationRepository.findByRoleId(r.getId()).size());
                    return resp;
                })
                .collect(Collectors.toList());
    }

    public RoleResponse getRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("角色不存在"));
        RoleResponse resp = RoleResponse.fromEntity(role);
        resp.setMenuIds(roleMenuRelationRepository.findByRoleId(role.getId()).stream()
                .map(RoleMenuRelation::getMenuId).collect(Collectors.toList()));
        resp.setPermissionIds(rolePermissionRelationRepository.findByRoleId(role.getId()).stream()
                .map(RolePermissionRelation::getPermissionId).collect(Collectors.toList()));
        return resp;
    }

    @Transactional
    public RoleResponse createRole(RoleCreateRequest request) {
        if (roleRepository.existsByCode(request.getCode())) {
            throw new BusinessException("角色编码已存在");
        }
        Role role = new Role();
        role.setName(request.getName());
        role.setCode(request.getCode());
        role.setDescription(request.getDescription());
        role.setType(request.getType() != null
                ? Role.RoleType.valueOf(request.getType()) : Role.RoleType.CUSTOM);
        role.setIsSystem(request.getSystem() != null ? request.getSystem() : false);
        role.setStatus(Role.RoleStatus.ACTIVE);
        role = roleRepository.save(role);
        log.info("创建角色: {} ({})", role.getName(), role.getCode());
        return RoleResponse.fromEntity(role);
    }

    @Transactional
    public RoleResponse updateRole(Long id, RoleUpdateRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("角色不存在"));
        if (role.getIsSystem()) {
            throw new BusinessException("系统角色不可修改");
        }
        if (request.getName() != null) role.setName(request.getName());
        if (request.getDescription() != null) role.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            role.setStatus(Role.RoleStatus.valueOf(request.getStatus()));
        }
        role = roleRepository.save(role);
        return RoleResponse.fromEntity(role);
    }

    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("角色不存在"));
        if (role.getIsSystem()) {
            throw new BusinessException("系统角色不可删除");
        }
        roleMenuRelationRepository.deleteByRoleId(id);
        rolePermissionRelationRepository.deleteByRoleId(id);
        roleRepository.delete(role);
        log.info("删除角色: {} ({})", role.getName(), role.getCode());
    }

    // ========== 用户-角色分配 ==========

    @Transactional
    public void assignUserRoles(RoleAssignRequest request) {
        Long userId = request.getUserId();
        userRepository.findById(userId).orElseThrow(() -> new BusinessException("用户不存在"));
        userRoleRelationRepository.deleteByUserId(userId);
        for (Long roleId : request.getRoleIds()) {
            roleRepository.findById(roleId).orElseThrow(() -> new BusinessException("角色不存在: " + roleId));
            UserRoleRelation ur = new UserRoleRelation();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            userRoleRelationRepository.save(ur);
        }
        log.info("为用户 {} 分配角色: {}", userId, request.getRoleIds());
    }

    public List<RoleResponse> getUserRoles(Long userId) {
        return roleRepository.findByUserId(userId).stream()
                .map(RoleResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ========== 角色-菜单分配 ==========

    @Transactional
    public void assignRoleMenus(RoleMenuAssignRequest request) {
        Long roleId = request.getRoleId();
        roleRepository.findById(roleId).orElseThrow(() -> new BusinessException("角色不存在"));
        roleMenuRelationRepository.deleteByRoleId(roleId);
        if (request.getMenuIds() != null) {
            for (Long menuId : request.getMenuIds()) {
                menuRepository.findById(menuId).orElseThrow(() -> new BusinessException("菜单不存在: " + menuId));
                RoleMenuRelation rm = new RoleMenuRelation();
                rm.setRoleId(roleId);
                rm.setMenuId(menuId);
                roleMenuRelationRepository.save(rm);
            }
        }
    }

    public List<MenuResponse> getRoleMenus(Long roleId) {
        return menuRepository.findByRoleId(roleId).stream()
                .map(MenuResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ========== 角色-权限分配 ==========

    @Transactional
    public void assignRolePermissions(RolePermissionAssignRequest request) {
        Long roleId = request.getRoleId();
        roleRepository.findById(roleId).orElseThrow(() -> new BusinessException("角色不存在"));
        rolePermissionRelationRepository.deleteByRoleId(roleId);
        if (request.getPermissionIds() != null) {
            for (Long permId : request.getPermissionIds()) {
                permissionRepository.findById(permId).orElseThrow(() -> new BusinessException("权限不存在: " + permId));
                RolePermissionRelation rp = new RolePermissionRelation();
                rp.setRoleId(roleId);
                rp.setPermissionId(permId);
                rolePermissionRelationRepository.save(rp);
            }
        }
    }

    public List<PermissionResponse> getRolePermissions(Long roleId) {
        return permissionRepository.findByRoleId(roleId).stream()
                .map(PermissionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ========== 权限管理 ==========

    public List<PermissionResponse> listPermissions() {
        return permissionRepository.findAll().stream()
                .map(PermissionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public PermissionResponse createPermission(PermissionCreateRequest request) {
        if (permissionRepository.existsByCode(request.getCode())) {
            throw new BusinessException("权限编码已存在");
        }
        Permission p = new Permission();
        p.setName(request.getName());
        p.setCode(request.getCode());
        p.setDescription(request.getDescription());
        p.setResourceType(Permission.ResourceType.valueOf(request.getResourceType()));
        p.setMethod(request.getMethod());
        p.setUrlPattern(request.getUrlPattern());
        p.setStatus(Permission.PermissionStatus.ACTIVE);
        p = permissionRepository.save(p);
        return PermissionResponse.fromEntity(p);
    }

    @Transactional
    public void deletePermission(Long id) {
        permissionRepository.deleteById(id);
    }

    // ========== 菜单管理 ==========

    public List<MenuResponse> listMenus() {
        List<Menu> allMenus = menuRepository.findByStatusOrderBySortOrderAsc(Menu.MenuStatus.ACTIVE);
        return buildMenuTree(allMenus);
    }

    public List<MenuResponse> getUserMenus(Long userId) {
        List<Menu> menus = menuRepository.findByUserId(userId);
        return buildMenuTree(menus);
    }

    private List<MenuResponse> buildMenuTree(List<Menu> menus) {
        List<MenuResponse> roots = new ArrayList<>();
        for (Menu menu : menus) {
            if (menu.getParentId() == null) {
                MenuResponse node = MenuResponse.fromEntity(menu);
                node.setChildren(buildChildren(menu.getId(), menus));
                roots.add(node);
            }
        }
        return roots;
    }

    private List<MenuResponse> buildChildren(Long parentId, List<Menu> allMenus) {
        List<MenuResponse> children = new ArrayList<>();
        for (Menu menu : allMenus) {
            if (parentId.equals(menu.getParentId())) {
                MenuResponse node = MenuResponse.fromEntity(menu);
                node.setChildren(buildChildren(menu.getId(), allMenus));
                children.add(node);
            }
        }
        return children;
    }

    @Transactional
    public MenuResponse createMenu(MenuCreateRequest request) {
        Menu menu = new Menu();
        menu.setName(request.getName());
        menu.setRoute(request.getRoute());
        menu.setIcon(request.getIcon());
        menu.setParentId(request.getParentId());
        menu.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        menu.setMenuType(request.getMenuType() != null
                ? Menu.MenuType.valueOf(request.getMenuType()) : Menu.MenuType.MENU);
        menu.setVisible(request.getVisible() != null ? request.getVisible() : true);
        menu.setPermissionCode(request.getPermissionCode());
        menu.setStatus(Menu.MenuStatus.ACTIVE);
        menu = menuRepository.save(menu);
        return MenuResponse.fromEntity(menu);
    }

    @Transactional
    public void deleteMenu(Long id) {
        List<Menu> children = menuRepository.findByParentIdOrderBySortOrderAsc(id);
        if (!children.isEmpty()) {
            throw new BusinessException("存在子菜单，请先删除子菜单");
        }
        menuRepository.deleteById(id);
    }

    // ========== 用户管理（系统管理用） ==========

    public List<UserResponse> listSystemUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
