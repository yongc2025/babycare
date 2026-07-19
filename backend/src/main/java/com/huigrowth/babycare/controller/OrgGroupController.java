package com.huigrowth.babycare.controller;

import com.huigrowth.babycare.aspect.AuditLogAnnotation;
import com.huigrowth.babycare.dto.OrgGroupCreateRequest;
import com.huigrowth.babycare.dto.OrgGroupResponse;
import com.huigrowth.babycare.service.OrgGroupService;
import com.huigrowth.babycare.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 集团/品牌管理控制器
 */
@Tag(name = "集团管理", description = "集团/品牌 CRUD 接口")
@RestController
@RequestMapping("/admin/org-groups")
@RequiredArgsConstructor
public class OrgGroupController {

    private final OrgGroupService orgGroupService;

    @Operation(summary = "获取集团列表")
    @GetMapping
    public ApiResponse<List<OrgGroupResponse>> listAll() {
        return ApiResponse.success(orgGroupService.listAll());
    }

    @Operation(summary = "获取集团详情")
    @GetMapping("/{id}")
    public ApiResponse<OrgGroupResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(orgGroupService.getById(id));
    }

    @AuditLogAnnotation(action = "CREATE_ORG_GROUP", actionName = "创建集团品牌", targetType = "OrgGroup")
    @Operation(summary = "创建集团品牌")
    @PostMapping
    public ApiResponse<OrgGroupResponse> create(@Valid @RequestBody OrgGroupCreateRequest request) {
        return ApiResponse.success("创建成功", orgGroupService.create(request));
    }

    @AuditLogAnnotation(action = "UPDATE_ORG_GROUP", actionName = "更新集团品牌", targetType = "OrgGroup")
    @Operation(summary = "更新集团品牌")
    @PutMapping("/{id}")
    public ApiResponse<OrgGroupResponse> update(@PathVariable Long id, @Valid @RequestBody OrgGroupCreateRequest request) {
        return ApiResponse.success("更新成功", orgGroupService.update(id, request));
    }

    @AuditLogAnnotation(action = "DELETE_ORG_GROUP", actionName = "禁用集团品牌", targetType = "OrgGroup")
    @Operation(summary = "禁用集团品牌")
    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        orgGroupService.delete(id);
        return ApiResponse.success("禁用成功");
    }
}
