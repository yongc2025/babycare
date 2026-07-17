package com.huigrowth.babycare.service;

import com.huigrowth.babycare.dto.BillingPaymentRequest;
import com.huigrowth.babycare.dto.BillingStatementCreateRequest;
import com.huigrowth.babycare.dto.BillingStatementResponse;
import com.huigrowth.babycare.dto.FeeItemRequest;
import com.huigrowth.babycare.dto.FeeItemResponse;
import com.huigrowth.babycare.entity.Baby;
import com.huigrowth.babycare.entity.BillingStatement;
import com.huigrowth.babycare.entity.Enrollment;
import com.huigrowth.babycare.entity.FeeItem;
import com.huigrowth.babycare.entity.Organization;
import com.huigrowth.babycare.entity.User;
import com.huigrowth.babycare.exception.BusinessException;
import com.huigrowth.babycare.repository.BabyRepository;
import com.huigrowth.babycare.repository.BillingStatementRepository;
import com.huigrowth.babycare.repository.EnrollmentRepository;
import com.huigrowth.babycare.repository.FamilyMemberRepository;
import com.huigrowth.babycare.repository.FeeItemRepository;
import com.huigrowth.babycare.repository.OrganizationRepository;
import com.huigrowth.babycare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillingService {

    private final FeeItemRepository feeItemRepository;
    private final BillingStatementRepository billingRepository;
    private final OrganizationRepository organizationRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final BabyRepository babyRepository;
    private final UserRepository userRepository;
    private final FamilyMemberRepository familyMemberRepository;

    @Transactional
    public FeeItemResponse createFeeItem(String username, FeeItemRequest request) {
        User operator = getUser(username);
        Organization organization = getOwnedOrganization(operator, request.getOrganizationId());
        FeeItem item = new FeeItem();
        item.setOrganization(organization);
        applyFeeItemFields(item, request);
        return convertFeeItem(feeItemRepository.save(item));
    }

    @Transactional
    public FeeItemResponse updateFeeItem(String username, Long feeItemId, FeeItemRequest request) {
        User operator = getUser(username);
        FeeItem item = feeItemRepository.findById(feeItemId)
                .orElseThrow(() -> new BusinessException("收费项目不存在"));
        if (!canAccessOrganization(operator, item.getOrganization().getId())) {
            throw new BusinessException("您无权操作该收费项目");
        }
        applyFeeItemFields(item, request);
        return convertFeeItem(feeItemRepository.save(item));
    }

    public List<FeeItemResponse> getOrganizationFeeItems(String username, Long organizationId) {
        User operator = getUser(username);
        Organization organization = getOwnedOrganization(operator, organizationId);
        return feeItemRepository.findByOrganizationOrderByCreatedAtDesc(organization).stream()
                .map(this::convertFeeItem)
                .collect(Collectors.toList());
    }

    @Transactional
    public BillingStatementResponse createBill(String username, BillingStatementCreateRequest request) {
        User operator = getUser(username);
        Enrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                .orElseThrow(() -> new BusinessException("入托档案不存在"));
        if (!canAccessOrganization(operator, enrollment.getOrganization().getId())) {
            throw new BusinessException("您无权为该宝宝生成账单");
        }

        FeeItem feeItem = null;
        if (request.getFeeItemId() != null) {
            feeItem = feeItemRepository.findById(request.getFeeItemId())
                    .orElseThrow(() -> new BusinessException("收费项目不存在"));
            if (!feeItem.getOrganization().getId().equals(enrollment.getOrganization().getId())) {
                throw new BusinessException("收费项目不属于该机构");
            }
        }

        BillingStatement bill = new BillingStatement();
        bill.setOrganization(enrollment.getOrganization());
        bill.setEnrollment(enrollment);
        bill.setFeeItem(feeItem);
        bill.setTitle(resolveBillTitle(request, feeItem));
        bill.setAmount(resolveBillAmount(request, feeItem));
        bill.setDueDate(request.getDueDate());
        bill.setRemark(request.getRemark());
        bill.setCreatedBy(operator);
        return convertBill(billingRepository.save(bill));
    }

    @Transactional
    public BillingStatementResponse markPaid(String username, Long billId, BillingPaymentRequest request) {
        User operator = getUser(username);
        BillingStatement bill = getOwnedBill(operator, billId);
        bill.setStatus(BillingStatement.BillingStatus.PAID);
        bill.setPaidAt(LocalDateTime.now());
        bill.setPaidBy(operator);
        bill.setPaymentMethod(request.getPaymentMethod());
        if (request.getRemark() != null) {
            bill.setRemark(request.getRemark());
        }
        return convertBill(billingRepository.save(bill));
    }

    @Transactional
    public BillingStatementResponse cancelBill(String username, Long billId, BillingPaymentRequest request) {
        User operator = getUser(username);
        BillingStatement bill = getOwnedBill(operator, billId);
        bill.setStatus(BillingStatement.BillingStatus.CANCELLED);
        if (request.getRemark() != null) {
            bill.setRemark(request.getRemark());
        }
        return convertBill(billingRepository.save(bill));
    }

    public List<BillingStatementResponse> getOrganizationBills(String username, Long organizationId) {
        User operator = getUser(username);
        Organization organization = getOwnedOrganization(operator, organizationId);
        return billingRepository.findByOrganizationOrderByCreatedAtDesc(organization).stream()
                .map(this::convertBill)
                .collect(Collectors.toList());
    }

    public List<BillingStatementResponse> getBabyBills(String username, Long babyId) {
        User operator = getUser(username);
        Baby baby = babyRepository.findById(babyId)
                .orElseThrow(() -> new BusinessException("宝宝不存在"));
        if (!canAccessBaby(operator, baby) && enrollmentRepository.findByBabyOrderByCreatedAtDesc(baby).stream()
                .noneMatch(enrollment -> canAccessOrganization(operator, enrollment.getOrganization().getId()))) {
            throw new BusinessException("您无权访问该宝宝账单");
        }
        return billingRepository.findByEnrollmentBabyOrderByCreatedAtDesc(baby).stream()
                .map(this::convertBill)
                .collect(Collectors.toList());
    }

    private BillingStatement getOwnedBill(User operator, Long billId) {
        BillingStatement bill = billingRepository.findById(billId)
                .orElseThrow(() -> new BusinessException("账单不存在"));
        if (!canAccessOrganization(operator, bill.getOrganization().getId())) {
            throw new BusinessException("您无权操作该账单");
        }
        return bill;
    }

    private Organization getOwnedOrganization(User operator, Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new BusinessException("机构不存在"));
        if (!canAccessOrganization(operator, organizationId)) {
            throw new BusinessException("您无权访问该机构");
        }
        return organization;
    }

    private void applyFeeItemFields(FeeItem item, FeeItemRequest request) {
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setAmount(request.getAmount());
        if (StringUtils.hasText(request.getStatus())) {
            item.setStatus(parseFeeItemStatus(request.getStatus()));
        }
    }

    private String resolveBillTitle(BillingStatementCreateRequest request, FeeItem feeItem) {
        if (StringUtils.hasText(request.getTitle())) {
            return request.getTitle();
        }
        if (feeItem != null) {
            return feeItem.getName();
        }
        return "托育账单";
    }

    private BigDecimal resolveBillAmount(BillingStatementCreateRequest request, FeeItem feeItem) {
        if (request.getAmount() != null) {
            return request.getAmount();
        }
        if (feeItem != null) {
            return feeItem.getAmount();
        }
        throw new BusinessException("账单金额不能为空");
    }

    private FeeItem.FeeItemStatus parseFeeItemStatus(String status) {
        try {
            return FeeItem.FeeItemStatus.valueOf(status);
        } catch (IllegalArgumentException error) {
            throw new BusinessException("收费项目状态不正确");
        }
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    private boolean canAccessBaby(User user, Baby baby) {
        return familyMemberRepository.existsByUserAndBaby(user, baby.getFamily().getId());
    }

    private boolean canAccessOrganization(User user, Long organizationId) {
        return organizationRepository.existsByIdAndCreatedBy(organizationId, user);
    }

    private FeeItemResponse convertFeeItem(FeeItem item) {
        FeeItemResponse response = new FeeItemResponse();
        response.setId(item.getId());
        response.setOrganizationId(item.getOrganization().getId());
        response.setOrganizationName(item.getOrganization().getName());
        response.setName(item.getName());
        response.setDescription(item.getDescription());
        response.setAmount(item.getAmount());
        response.setStatus(item.getStatus());
        response.setStatusDescription(item.getStatus().getDescription());
        response.setCreatedAt(item.getCreatedAt());
        response.setUpdatedAt(item.getUpdatedAt());
        return response;
    }

    private BillingStatementResponse convertBill(BillingStatement bill) {
        BillingStatementResponse response = new BillingStatementResponse();
        response.setId(bill.getId());
        response.setOrganizationId(bill.getOrganization().getId());
        response.setOrganizationName(bill.getOrganization().getName());
        response.setEnrollmentId(bill.getEnrollment().getId());
        response.setBabyId(bill.getEnrollment().getBaby().getId());
        response.setBabyName(bill.getEnrollment().getBaby().getName());
        if (bill.getFeeItem() != null) {
            response.setFeeItemId(bill.getFeeItem().getId());
            response.setFeeItemName(bill.getFeeItem().getName());
        }
        response.setTitle(bill.getTitle());
        response.setAmount(bill.getAmount());
        response.setDueDate(bill.getDueDate());
        response.setStatus(bill.getStatus());
        response.setStatusDescription(bill.getStatus().getDescription());
        response.setPaidAt(bill.getPaidAt());
        response.setPaymentMethod(bill.getPaymentMethod());
        response.setRemark(bill.getRemark());
        if (bill.getCreatedBy() != null) {
            response.setCreatedById(bill.getCreatedBy().getId());
            response.setCreatedByName(bill.getCreatedBy().getNickname());
        }
        if (bill.getPaidBy() != null) {
            response.setPaidById(bill.getPaidBy().getId());
            response.setPaidByName(bill.getPaidBy().getNickname());
        }
        response.setCreatedAt(bill.getCreatedAt());
        response.setUpdatedAt(bill.getUpdatedAt());
        return response;
    }
}
