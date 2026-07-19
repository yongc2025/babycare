package com.huigrowth.babycare.controller;

import com.huigrowth.babycare.aspect.AuditLogAnnotation;
import com.huigrowth.babycare.dto.DataDictCreateRequest;
import com.huigrowth.babycare.dto.DataDictResponse;
import com.huigrowth.babycare.service.DataDictService;
import com.huigrowth.babycare.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据字典控制器
 */
@Tag(name = "数据字典", description = "数据字典管理接口")
@RestController
@RequestMapping("/admin/data-dict")
@RequiredArgsConstructor
public class DataDictController {

    private final DataDictService dataDictService;

    @Operation(summary = "获取字典类型列表")
    @GetMapping("/types")
    public ApiResponse<List<String>> listTypes() {
        return ApiResponse.success(dataDictService.listDictTypes());
    }

    @Operation(summary = "获取指定类型的字典项")
    @GetMapping("/type/{dictType}")
    public ApiResponse<List<DataDictResponse>> listByType(@PathVariable String dictType) {
        return ApiResponse.success(dataDictService.listByType(dictType));
    }

    @AuditLogAnnotation(action = "CREATE_DATA_DICT", actionName = "创建字典项", targetType = "DataDict")
    @Operation(summary = "创建字典项")
    @PostMapping
    public ApiResponse<DataDictResponse> create(@Valid @RequestBody DataDictCreateRequest request) {
        return ApiResponse.success("创建成功", dataDictService.create(request));
    }

    @AuditLogAnnotation(action = "UPDATE_DATA_DICT", actionName = "更新字典项", targetType = "DataDict")
    @Operation(summary = "更新字典项")
    @PutMapping("/{id}")
    public ApiResponse<DataDictResponse> update(@PathVariable Long id, @Valid @RequestBody DataDictCreateRequest request) {
        return ApiResponse.success("更新成功", dataDictService.update(id, request));
    }

    @AuditLogAnnotation(action = "DELETE_DATA_DICT", actionName = "删除字典项", targetType = "DataDict")
    @Operation(summary = "删除字典项")
    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        dataDictService.delete(id);
        return ApiResponse.success("删除成功");
    }
}
