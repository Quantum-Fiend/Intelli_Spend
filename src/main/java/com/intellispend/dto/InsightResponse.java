package com.intellispend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsightResponse {
    private Map<String, BigDecimal> categoryTotals;
    private BigDecimal totalSpending;
    private BigDecimal previousMonthTotal;
    private Double monthOverMonthPercentage;
    private String aiSummary;
    private java.util.Map<java.time.LocalDate, java.math.BigDecimal> dailySpending;
    private java.util.Map<Integer, java.math.BigDecimal> weeklySpending;
}
