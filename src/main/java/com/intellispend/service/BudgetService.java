package com.intellispend.service;

import com.intellispend.entity.Budget;
import com.intellispend.entity.Expense;
import com.intellispend.entity.User;
import com.intellispend.repository.BudgetRepository;
import com.intellispend.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetService {
    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;

    public void checkBudget(Expense expense) {
        User user = expense.getUser();
        String category = expense.getCategory();
        YearMonth month = YearMonth.from(expense.getDate());

        Optional<Budget> budgetOpt = budgetRepository.findByUserAndCategoryAndMonth(user, category, month);
        if (budgetOpt.isPresent()) {
            Budget budget = budgetOpt.get();
            BigDecimal totalSpent = expenseRepository.findAllByUser(user).stream()
                    .filter(e -> e.getCategory().equals(category) && YearMonth.from(e.getDate()).equals(month))
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalSpent.compareTo(budget.getAmount()) > 0) {
                log.warn("BUDGET ALERT: Category {} for user {} exceeded budget of {}. Current spending: {}",
                        category, user.getUsername(), budget.getAmount(), totalSpent);
            } else if (totalSpent.compareTo(budget.getAmount().multiply(new BigDecimal("0.9"))) > 0) {
                log.info("BUDGET WARNING: Category {} for user {} is over 90% of budget. Current spending: {}",
                        category, user.getUsername(), totalSpent);
            }
        }
    }
}
