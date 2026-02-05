package com.intellispend.controller;

import com.intellispend.dto.InsightResponse;
import com.intellispend.service.InsightService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/v1/insights")
@RequiredArgsConstructor
public class InsightController {
    private final InsightService insightService;

    @GetMapping
    public ResponseEntity<InsightResponse> getInsights(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            Authentication authentication) {
        
        YearMonth targetMonth = (month != null) ? month : YearMonth.now();
        return ResponseEntity.ok(insightService.getMonthlyInsights(authentication.getName(), targetMonth));
    }
}
