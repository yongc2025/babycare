package com.huigrowth.babycare.controller;

import com.huigrowth.babycare.dto.*;
import com.huigrowth.babycare.service.RbacService;
import com.huigrowth.babycare.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RBAC 权限管理控制器
 */
@Tag(name = "权限管理", description = "角色、权限、菜单、用户角色分配等 RBAC 接口")
@RestController
@RequestMapping("/api/admin/rbac")
@RequiredArgsConstructor
public class RbacController {

    private final RbacService rbacService;

    // ========== 角色管理 ==========

    @Operation(summary = "获取角色列表")
    @GetMapping("/roles")
    public ApiResponse<List<RoleResponse>> listRoles() {
        return ApiResponse.success(rbacService.listRoles());
    }

    @Operation(summary = "获取角色详情")
    @GetMapping("/roles/{id}")
    public ApiResponse<RoleResponse> getRole(@PathVariable Long id) {
        return ApiResponse.success(rbacService.getRole(id));
    }

    @Operation(summary = "创建角色")
    @PostMapping("/roles")
    public ApiResponse<RoleResponse> createRole(@Valid @RequestBody RoleCreateRequest request) {
        return ApiResponse.success("创建成功", rbacService.createRole(request));
    }

    @Operation(summary = "更新角色")
    @PutMapping("/roles/{id}")
    public ApiResponse<RoleResponse> updateRole(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
        return ApiResponse.success("更新成功", rbacService.updateRole(id, request));
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/roles/{id}")
    public ApiResponse<String> deleteRole(@PathVariable Long id) {
        rbacService.deleteRole(id);
        return ApiResponse.success("删除成功");
    }

    // ========== 用户-角色分配 ==========

    @Operation(summary = "分配用户角色")
    @PostMapping("/user-roles")
    public ApiResponse<String> assignUserRoles(@Valid @RequestBody RoleAssignRequest request) {
        rbacService.assignUserRoles(request);
        return ApiResponse.success("分配成功");
    }

    @Operation(summary = "获取用户角色")
    @GetMapping("/user-roles/{userId}")
    public ApiResponse<List<RoleResponse>> getUserRoles(@PathVariable Long userId) {
        return ApiResponse.success(rbacService.getUserRoles(userId));
    }

    // ========== 角色-菜单分配 ==========

    @Operation(summary = "分配角色菜单")
    @PostMapping("/role-menus")
    public ApiResponse<String> assignRoleMenus(@Valid @RequestBody RoleMenuAssignRequest request) {
        rbacService.assignRoleMenus(request);
        return ApiResponse.success("分配成功");
    }

    @Operation(summary = "获取角色菜单")
    @GetMapping("/role-menus/{roleId}")
    public ApiResponse<List<MenuResponse>> getRoleMenus(@PathVariable Long roleId) {
        return ApiResponse.success(rbacService.getRoleMenus(roleId));
    }

    // ========== 角色-权限分配 ==========

    @Operation(summary = "分配角色权限")
    @PostMapping("/role-permissions")
    public ApiResponse<String> assignRolePermissions(@Valid @RequestBody RolePermissionAssignRequest request) {
        rbacService.assignRolePermissions(request);
        return ApiResponse.success("分配成功");
    }

    @Operation(summary = "获取角色权限")
    @GetMapping("/role-permissions/{roleId}")
    public ApiResponse<List<PermissionResponse>> getRolePermissions(@PathVariable Long roleId) {
        return ApiResponse.success(rbacService.getRolePermissions(roleId));
    }

    // ========== 权限管理 ==========

    @Operation(summary = "获取权限列表")
    @GetMapping("/permissions")
    public ApiResponse<List<PermissionResponse>> listPermissions() {
        return ApiResponse.success(rbacService.listPermissions());
    }

    @Operation(summary = "创建权限")
    @PostMapping("/permissions")
    public ApiResponse<PermissionResponse> createPermission(@Valid @RequestBody PermissionCreateRequest request) {
        return ApiResponse.success("创建成功", rbacService.createPermission(request));
    }

    @Operation(summary = "删除权限")
    @DeleteMapping("/permissions/{id}")
    public ApiResponse<String> deletePermission(@PathVariable Long id) {
        rbacService.deletePermission(id);
        return ApiResponse.success("删除成功");
    }

    // ========== 菜单管理 ==========

    @Operation(summary = "获取菜单树")
    @GetMapping("/menus")
    public ApiResponse<List<MenuResponse>> listMenus() {
        return ApiResponse.success(rbacService.listMenus());
    }

    @Operation(summary = "获取用户菜单树")
    @GetMapping("/menues/user")
    public ApiResponse<List<MenuResponse>> getUserMenus(@RequestParam Long userId) {
        return ApiResponse.success(rbacService.getUserMenus(userId));
    }

    @Operation(summary = "创建菜单")
    @PostMapping("/menus")
    public ApiResponse<MenuResponse> createMenu(@Valid @RequestBody MenuCreateRequest request) {
        return ApiResponse.success("创建成功", rbacService.createMenu(request));
    }

    @Operation(summary = "删除菜单")
    @DeleteMapping("/menus/{id}")
    public ApiResponse<String> deleteMenu(@PathVariable Long id) {
        rbacService.deleteMenu(id);
        return ApiResponse.success("删除成功");
    }

    // ========== 用户管理（系统管理用） ==========

    @Operation(summary = "获取用户列表")
    @GetMapping("/users")
    public ApiResponse<List<UserResponse>> listUsers() {
        return ApiResponse.success(rbacService.listSystemUsers());
    }
}
