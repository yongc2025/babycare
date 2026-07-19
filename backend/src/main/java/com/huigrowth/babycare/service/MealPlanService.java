package com.huigrowth.babycare.service;

import com.huigrowth.babycare.dto.MealIntakeRequest;
import com.huigrowth.babycare.dto.MealIntakeResponse;
import com.huigrowth.babycare.dto.MealNutritionAnalysisResponse;
import com.huigrowth.babycare.dto.MealPlanRequest;
import com.huigrowth.babycare.dto.MealPlanResponse;
import com.huigrowth.babycare.dto.MealNutritionAnalysisResponse.MealIntakeStats;
import com.huigrowth.babycare.entity.Enrollment;
import com.huigrowth.babycare.entity.MealIntakeRecord;
import com.huigrowth.babycare.entity.MealPlan;
import com.huigrowth.babycare.entity.Organization;
import com.huigrowth.babycare.entity.User;
import com.huigrowth.babycare.exception.BusinessException;
import com.huigrowth.babycare.repository.EnrollmentRepository;
import com.huigrowth.babycare.repository.MealIntakeRecordRepository;
import com.huigrowth.babycare.repository.MealPlanRepository;
import com.huigrowth.babycare.repository.OrganizationRepository;
import com.huigrowth.babycare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MealPlanService {

    private final MealPlanRepository mealPlanRepository;
    private final MealIntakeRecordRepository intakeRecordRepository;
    private final OrganizationRepository organizationRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    @Transactional
    public MealPlanResponse createMealPlan(String username, MealPlanRequest request) {
        User operator = getUser(username);
        Organization organization = getOwnedOrganization(operator, request.getOrganizationId());
        MealPlan mealPlan = new MealPlan();
        mealPlan.setOrganization(organization);
        mealPlan.setCreatedBy(operator);
        applyMealPlanFields(mealPlan, request);
        return convertMealPlan(mealPlanRepository.save(mealPlan), false);
    }

    @Transactional
    public MealPlanResponse updateMealPlan(String username, Long mealPlanId, MealPlanRequest request) {
        User operator = getUser(username);
        MealPlan mealPlan = getOwnedMealPlan(operator, mealPlanId);
        applyMealPlanFields(mealPlan, request);
        return convertMealPlan(mealPlanRepository.save(mealPlan), false);
    }

    @Transactional
    public MealPlanResponse publishMealPlan(String username, Long mealPlanId) {
        User operator = getUser(username);
        MealPlan mealPlan = getOwnedMealPlan(operator, mealPlanId);
        mealPlan.setStatus(MealPlan.MealPlanStatus.PUBLISHED);
        return convertMealPlan(mealPlanRepository.save(mealPlan), false);
    }

    public List<MealPlanResponse> getOrganizationMeals(
            String username,
            Long organizationId,
            LocalDate date,
            LocalDate startDate,
            LocalDate endDate) {
        User operator = getUser(username);
        Organization organization = getOwnedOrganization(operator, organizationId);
        if (startDate != null || endDate != null) {
            LocalDate start = startDate != null ? startDate : LocalDate.now();
            LocalDate end = endDate != null ? endDate : start;
            if (start.isAfter(end)) {
                throw new BusinessException("开始日期不能晚于结束日期");
            }
            return mealPlanRepository
                    .findByOrganizationAndMealDateBetweenOrderByMealDateAscMealTypeAsc(organization, start, end)
                    .stream()
                    .map(mealPlan -> convertMealPlan(mealPlan, false))
                    .collect(Collectors.toList());
        }
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return mealPlanRepository.findByOrganizationAndMealDateOrderByMealTypeAsc(organization, targetDate).stream()
                .map(mealPlan -> convertMealPlan(mealPlan, false))
                .collect(Collectors.toList());
    }

    @Transactional
    public MealIntakeResponse recordIntake(String username, MealIntakeRequest request) {
        User operator = getUser(username);
        MealPlan mealPlan = getOwnedMealPlan(operator, request.getMealPlanId());
        Enrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                .orElseThrow(() -> new BusinessException("入托档案不存在"));
        if (!enrollment.getOrganization().getId().equals(mealPlan.getOrganization().getId())) {
            throw new BusinessException("进食记录不属于该食谱机构");
        }
        if (!canAccessOrganization(operator, enrollment.getOrganization().getId())) {
            throw new BusinessException("您无权记录该宝宝进食情况");
        }
        MealIntakeRecord record = intakeRecordRepository.findByMealPlanAndEnrollment(mealPlan, enrollment)
                .orElseGet(MealIntakeRecord::new);
        record.setMealPlan(mealPlan);
        record.setEnrollment(enrollment);
        record.setRecordedBy(operator);
        applyIntakeFields(record, request);
        return convertIntake(intakeRecordRepository.save(record));
    }

    public List<MealIntakeResponse> getMealIntakes(String username, Long mealPlanId) {
        User operator = getUser(username);
        MealPlan mealPlan = getOwnedMealPlan(operator, mealPlanId);
        return intakeRecordRepository.findByMealPlanOrderByCreatedAtDesc(mealPlan).stream()
                .map(this::convertIntake)
                .collect(Collectors.toList());
    }

    public List<MealIntakeResponse> getEnrollmentIntakes(String username, Long enrollmentId) {
        User operator = getUser(username);
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new BusinessException("入托档案不存在"));
        if (!canAccessOrganization(operator, enrollment.getOrganization().getId())) {
            throw new BusinessException("您无权查看该宝宝进食记录");
        }
        return intakeRecordRepository.findByEnrollmentOrderByCreatedAtDesc(enrollment).stream()
                .map(this::convertIntake)
                .collect(Collectors.toList());
    }

    public MealNutritionAnalysisResponse getNutritionAnalysis(String username, Long organizationId, LocalDate startDate, LocalDate endDate) {
        User operator = getUser(username);
        Organization organization = getOwnedOrganization(operator, organizationId);
        if (startDate == null) startDate = LocalDate.now().minusDays(7);
        if (endDate == null) endDate = LocalDate.now();
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("开始日期不能晚于结束日期");
        }

        List<MealPlan> meals = mealPlanRepository.findByOrganizationAndMealDateBetweenOrderByMealDateAscMealTypeAsc(
                organization, startDate, endDate);

        MealNutritionAnalysisResponse response = new MealNutritionAnalysisResponse();
        response.setTotalMeals(meals.size());

        List<MealIntakeStats> mealStats = meals.stream().map(meal -> {
            List<MealIntakeRecord> intakes = intakeRecordRepository.findByMealPlanOrderByCreatedAtDesc(meal);
            MealIntakeStats stats = new MealIntakeStats();
            stats.setMealPlanId(meal.getId());
            stats.setMealDate(meal.getMealDate());
            stats.setMealType(meal.getMealType().name());
            stats.setMealTypeDescription(meal.getMealType().getDescription());
            stats.setTitle(meal.getTitle());
            stats.setFoodItems(meal.getFoodItems());
            stats.setAllergenNotes(meal.getAllergenNotes());
            stats.setTotalBabies(intakes.size());
            stats.setAllCount((int) intakes.stream().filter(r -> r.getIntakeLevel() == MealIntakeRecord.IntakeLevel.ALL).count());
            stats.setMostCount((int) intakes.stream().filter(r -> r.getIntakeLevel() == MealIntakeRecord.IntakeLevel.MOST).count());
            stats.setHalfCount((int) intakes.stream().filter(r -> r.getIntakeLevel() == MealIntakeRecord.IntakeLevel.HALF).count());
            stats.setLessCount((int) intakes.stream().filter(r -> r.getIntakeLevel() == MealIntakeRecord.IntakeLevel.LESS).count());
            stats.setNoneCount((int) intakes.stream().filter(r -> r.getIntakeLevel() == MealIntakeRecord.IntakeLevel.NONE).count());
            stats.setAllergyCount((int) intakes.stream().filter(MealIntakeRecord::getAllergyReaction).count());

            // Calculate weighted average intake rate
            int totalScore = stats.getAllCount() * 100 + stats.getMostCount() * 75
                    + stats.getHalfCount() * 50 + stats.getLessCount() * 25;
            stats.setAvgIntakeRate(intakes.isEmpty() ? 0 : Math.round((double) totalScore / intakes.size() * 10.0) / 10.0);

            return stats;
        }).collect(Collectors.toList());

        response.setMealStats(mealStats);
        response.setTotalIntakeRecords(mealStats.stream().mapToInt(MealIntakeStats::getTotalBabies).sum());
        response.setTotalBabies((int) meals.stream()
                .flatMap(m -> intakeRecordRepository.findByMealPlanOrderByCreatedAtDesc(m).stream())
                .map(r -> r.getEnrollment().getId())
                .distinct()
                .count());
        response.setAllergyEventCount(mealStats.stream().mapToInt(MealIntakeStats::getAllergyCount).sum());

        return response;
    }

    private void applyMealPlanFields(MealPlan mealPlan, MealPlanRequest request) {
        mealPlan.setMealDate(request.getMealDate());
        mealPlan.setMealType(parseMealType(request.getMealType()));
        mealPlan.setTitle(request.getTitle());
        mealPlan.setFoodItems(request.getFoodItems());
        mealPlan.setAllergenNotes(request.getAllergenNotes());
        mealPlan.setNutritionNotes(request.getNutritionNotes());
        if (StringUtils.hasText(request.getStatus())) {
            mealPlan.setStatus(parseStatus(request.getStatus()));
        }
    }

    private void applyIntakeFields(MealIntakeRecord record, MealIntakeRequest request) {
        if (StringUtils.hasText(request.getIntakeLevel())) {
            record.setIntakeLevel(parseIntakeLevel(request.getIntakeLevel()));
        }
        record.setAllergyReaction(Boolean.TRUE.equals(request.getAllergyReaction()));
        record.setReactionNotes(request.getReactionNotes());
        record.setRemark(request.getRemark());
    }

    private MealPlan getOwnedMealPlan(User operator, Long mealPlanId) {
        MealPlan mealPlan = mealPlanRepository.findById(mealPlanId)
                .orElseThrow(() -> new BusinessException("食谱不存在"));
        if (!canAccessOrganization(operator, mealPlan.getOrganization().getId())) {
            throw new BusinessException("您无权操作该食谱");
        }
        return mealPlan;
    }

    private Organization getOwnedOrganization(User operator, Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new BusinessException("机构不存在"));
        if (!canAccessOrganization(operator, organizationId)) {
            throw new BusinessException("您无权访问该机构");
        }
        return organization;
    }

    private boolean canAccessOrganization(User operator, Long organizationId) {
        return organizationRepository.existsByIdAndCreatedBy(organizationId, operator);
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    private MealPlan.MealType parseMealType(String mealType) {
        try {
            return MealPlan.MealType.valueOf(mealType);
        } catch (IllegalArgumentException error) {
            throw new BusinessException("餐次类型不正确");
        }
    }

    private MealPlan.MealPlanStatus parseStatus(String status) {
        try {
            return MealPlan.MealPlanStatus.valueOf(status);
        } catch (IllegalArgumentException error) {
            throw new BusinessException("食谱状态不正确");
        }
    }

    private MealIntakeRecord.IntakeLevel parseIntakeLevel(String level) {
        try {
            return MealIntakeRecord.IntakeLevel.valueOf(level);
        } catch (IllegalArgumentException error) {
            throw new BusinessException("进食量状态不正确");
        }
    }

    private MealPlanResponse convertMealPlan(MealPlan mealPlan, boolean withIntakes) {
        MealPlanResponse response = new MealPlanResponse();
        response.setId(mealPlan.getId());
        response.setOrganizationId(mealPlan.getOrganization().getId());
        response.setOrganizationName(mealPlan.getOrganization().getName());
        response.setMealDate(mealPlan.getMealDate());
        response.setMealType(mealPlan.getMealType());
        response.setMealTypeDescription(mealPlan.getMealType().getDescription());
        response.setTitle(mealPlan.getTitle());
        response.setFoodItems(mealPlan.getFoodItems());
        response.setAllergenNotes(mealPlan.getAllergenNotes());
        response.setNutritionNotes(mealPlan.getNutritionNotes());
        response.setStatus(mealPlan.getStatus());
        response.setStatusDescription(mealPlan.getStatus().getDescription());
        if (mealPlan.getCreatedBy() != null) {
            response.setCreatedById(mealPlan.getCreatedBy().getId());
            response.setCreatedByName(mealPlan.getCreatedBy().getNickname());
        }
        if (withIntakes) {
            response.setIntakeRecords(intakeRecordRepository.findByMealPlanOrderByCreatedAtDesc(mealPlan).stream()
                    .map(this::convertIntake)
                    .collect(Collectors.toList()));
        }
        response.setCreatedAt(mealPlan.getCreatedAt());
        response.setUpdatedAt(mealPlan.getUpdatedAt());
        return response;
    }

    private MealIntakeResponse convertIntake(MealIntakeRecord record) {
        MealIntakeResponse response = new MealIntakeResponse();
        response.setId(record.getId());
        response.setMealPlanId(record.getMealPlan().getId());
        response.setEnrollmentId(record.getEnrollment().getId());
        response.setBabyId(record.getEnrollment().getBaby().getId());
        response.setBabyName(record.getEnrollment().getBaby().getName());
        response.setIntakeLevel(record.getIntakeLevel());
        response.setIntakeLevelDescription(record.getIntakeLevel().getDescription());
        response.setAllergyReaction(record.getAllergyReaction());
        response.setReactionNotes(record.getReactionNotes());
        response.setRemark(record.getRemark());
        if (record.getRecordedBy() != null) {
            response.setRecordedById(record.getRecordedBy().getId());
            response.setRecordedByName(record.getRecordedBy().getNickname());
        }
        response.setCreatedAt(record.getCreatedAt());
        response.setUpdatedAt(record.getUpdatedAt());
        return response;
    }
}
