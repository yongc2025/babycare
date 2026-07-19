package com.huigrowth.babycare.controller;

import com.huigrowth.babycare.dto.BillingPaymentRequest;
import com.huigrowth.babycare.dto.BillingStatementCreateRequest;
import com.huigrowth.babycare.dto.BillingStatementResponse;
import com.huigrowth.babycare.dto.FeeItemRequest;
import com.huigrowth.babycare.dto.FeeItemResponse;
import com.huigrowth.babycare.dto.FinanceWorkbenchResponse;
import com.huigrowth.babycare.service.BillingService;
import com.huigrowth.babycare.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "收费账单", description = "收费项目、账单生成和缴费状态接口")
@RestController
@RequestMapping("/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @Operation(summary = "创建收费项目")
    @PostMapping("/fee-item/create")
    public ApiResponse<FeeItemResponse> createFeeItem(
            Authentication authentication,
            @Valid @RequestBody FeeItemRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        FeeItemResponse response = billingService.createFeeItem(userDetails.getUsername(), request);
        return ApiResponse.success("收费项目创建成功", response);
    }

    @Operation(summary = "更新收费项目")
    @PutMapping("/fee-item/{feeItemId}")
    public ApiResponse<FeeItemResponse> updateFeeItem(
            Authentication authentication,
            @PathVariable Long feeItemId,
            @Valid @RequestBody FeeItemRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        FeeItemResponse response = billingService.updateFeeItem(userDetails.getUsername(), feeItemId, request);
        return ApiResponse.success("收费项目更新成功", response);
    }

    @Operation(summary = "机构收费项目列表")
    @GetMapping("/fee-item/organization/{organizationId}")
    public ApiResponse<List<FeeItemResponse>> getOrganizationFeeItems(
            Authentication authentication,
            @PathVariable Long organizationId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<FeeItemResponse> response = billingService.getOrganizationFeeItems(
                userDetails.getUsername(),
                organizationId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "生成账单")
    @PostMapping("/bill/create")
    public ApiResponse<BillingStatementResponse> createBill(
            Authentication authentication,
            @Valid @RequestBody BillingStatementCreateRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        BillingStatementResponse response = billingService.createBill(userDetails.getUsername(), request);
        return ApiResponse.success("账单生成成功", response);
    }

    @Operation(summary = "标记账单已支付")
    @PostMapping("/bill/{billId}/paid")
    public ApiResponse<BillingStatementResponse> markPaid(
            Authentication authentication,
            @PathVariable Long billId,
            @Valid @RequestBody BillingPaymentRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        BillingStatementResponse response = billingService.markPaid(userDetails.getUsername(), billId, request);
        return ApiResponse.success("账单已标记支付", response);
    }

    @Operation(summary = "取消账单")
    @PostMapping("/bill/{billId}/cancel")
    public ApiResponse<BillingStatementResponse> cancelBill(
            Authentication authentication,
            @PathVariable Long billId,
            @Valid @RequestBody BillingPaymentRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        BillingStatementResponse response = billingService.cancelBill(userDetails.getUsername(), billId, request);
        return ApiResponse.success("账单已取消", response);
    }

    @Operation(summary = "机构账单列表")
    @GetMapping("/bill/organization/{organizationId}")
    public ApiResponse<List<BillingStatementResponse>> getOrganizationBills(
            Authentication authentication,
            @PathVariable Long organizationId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<BillingStatementResponse> response = billingService.getOrganizationBills(
                userDetails.getUsername(),
                organizationId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "宝宝账单列表")
    @GetMapping("/bill/baby/{babyId}")
    public ApiResponse<List<BillingStatementResponse>> getBabyBills(
            Authentication authentication,
            @PathVariable Long babyId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<BillingStatementResponse> response = billingService.getBabyBills(userDetails.getUsername(), babyId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "财务运营工作台聚合数据")
    @GetMapping("/finance-workbench/{organizationId}")
    public ApiResponse<FinanceWorkbenchResponse> getFinanceWorkbench(
            Authentication authentication,
            @PathVariable Long organizationId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        FinanceWorkbenchResponse response = billingService.getFinanceWorkbench(
                userDetails.getUsername(), organizationId);
        return ApiResponse.success(response);
    }
}
