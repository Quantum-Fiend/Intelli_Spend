package com.intellispend.controller;

import com.intellispend.entity.Budget;
import com.intellispend.entity.User;
import com.intellispend.repository.BudgetRepository;
import com.intellispend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
public class BudgetController {
    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<Budget> setBudget(
            @RequestParam String category,
            @RequestParam BigDecimal amount,
            @RequestParam String month, // YYYY-MM
            Authentication authentication) {
        
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        YearMonth yearMonth = YearMonth.parse(month);
        
        Budget budget = budgetRepository.findByUserAndCategoryAndMonth(user, category, yearMonth)
                .orElse(Budget.builder()
                        .user(user)
                        .category(category)
                        .month(yearMonth)
                        .build());
        
        budget.setAmount(amount);
        return ResponseEntity.ok(budgetRepository.save(budget));
    }

    @GetMapping
    public ResponseEntity<List<Budget>> getBudgets(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(budgetRepository.findAllByUser(user));
    }
}
