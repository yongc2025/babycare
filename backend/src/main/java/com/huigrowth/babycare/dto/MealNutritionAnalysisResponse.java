package com.huigrowth.babycare.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 营养膳食分析响应
 * 包含机构在指定日期范围内的食谱进食统计
 */
@Data
public class MealNutritionAnalysisResponse {
    /** 日期范围内总食谱数 */
    private int totalMeals;
    /** 有进食记录的宝宝数 */
    private int totalBabies;
    /** 进食记录总数 */
    private int totalIntakeRecords;
    /** 过敏事件数 */
    private int allergyEventCount;
    /** 每餐进食统计 */
    private List<MealIntakeStats> mealStats;

    @Data
    public static class MealIntakeStats {
        private Long mealPlanId;
        private LocalDate mealDate;
        private String mealType;
        private String mealTypeDescription;
        private String title;
        private String foodItems;
        private String allergenNotes;
        /** 该餐次有进食记录的宝宝数 */
        private int totalBabies;
        /** 全部吃完人数 */
        private int allCount;
        /** 大部分吃完人数 */
        private int mostCount;
        /** 吃完一半人数 */
        private int halfCount;
        /** 少量进食人数 */
        private int lessCount;
        /** 拒食人数 */
        private int noneCount;
        /** 过敏事件数 */
        private int allergyCount;
        /** 平均进食率（0~100%），加权：ALL=100, MOST=75, HALF=50, LESS=25, NONE=0 */
        private double avgIntakeRate;
    }
}
