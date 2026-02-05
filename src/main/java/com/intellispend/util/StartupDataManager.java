package com.intellispend.util;

import com.intellispend.entity.User;
import com.intellispend.repository.ExpenseRepository;
import com.intellispend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class StartupDataManager implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final com.intellispend.repository.BudgetRepository budgetRepository;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            log.info("Seeding comprehensive demo data...");

            User demoUser = User.builder()
                    .username("demo")
                    .email("demo@example.com")
                    .password(encoder.encode("password123"))
                    .isDeleted(false)
                    .build();
            
            userRepository.save(Objects.requireNonNull(demoUser));

            // Seed Budgets
            if (budgetRepository.findAllByUser(demoUser).isEmpty()) {
                budgetRepository.save(java.util.Objects.requireNonNull(com.intellispend.entity.Budget.builder()
                        .user(demoUser)
                        .category("Food")
                        .amount(new BigDecimal("500.00"))
                        .month(java.time.YearMonth.now())
                        .build()));
                
                budgetRepository.save(java.util.Objects.requireNonNull(com.intellispend.entity.Budget.builder()
                        .user(demoUser)
                        .category("Housing")
                        .amount(new BigDecimal("3000.00"))
                        .month(java.time.YearMonth.now())
                        .build()));
            }

            // Seed Expenses
            if (expenseRepository.findAllByUser(demoUser).isEmpty()) {
                // Monthly Rent
                expenseRepository.save(Objects.requireNonNull(com.intellispend.entity.Expense.builder()
                        .amount(new BigDecimal("2500.00"))
                        .category("Housing")
                        .description("Monthly Rent")
                        .date(LocalDate.now().minusDays(5))
                        .paymentMethod("Bank Transfer")
                        .currency("USD")
                        .user(demoUser)
                        .build()));

                // Groceries (Food)
                expenseRepository.save(Objects.requireNonNull(com.intellispend.entity.Expense.builder()
                        .amount(new BigDecimal("120.50"))
                        .category("Food")
                        .description("Grocery shopping")
                        .date(LocalDate.now().minusDays(3))
                        .paymentMethod("Credit Card")
                        .currency("USD")
                        .user(demoUser)
                        .build()));

                // Dining Out (Food) - Close to budget limit
                expenseRepository.save(Objects.requireNonNull(com.intellispend.entity.Expense.builder()
                        .amount(new BigDecimal("320.00"))
                        .category("Food")
                        .description("Dinner at Steakhouse")
                        .date(LocalDate.now().minusDays(1))
                        .paymentMethod("Credit Card")
                        .currency("USD")
                        .user(demoUser)
                        .build()));

                // Fuel
                expenseRepository.save(Objects.requireNonNull(com.intellispend.entity.Expense.builder()
                        .amount(new BigDecimal("65.00"))
                        .category("Transport")
                        .description("Fuel refill")
                        .date(LocalDate.now().minusDays(1))
                        .paymentMethod("Debit Card")
                        .currency("USD")
                        .user(demoUser)
                        .build()));

                // Soft-deleted expense (for testing data integrity)
                com.intellispend.entity.Expense deletedExpense = com.intellispend.entity.Expense.builder()
                        .amount(new BigDecimal("99.99"))
                        .category("Entertainment")
                        .description("Canceled Concert")
                        .date(LocalDate.now().minusDays(10))
                        .paymentMethod("Credit Card")
                        .currency("USD")
                        .isDeleted(true)
                        .user(demoUser)
                        .build();
                expenseRepository.save(Objects.requireNonNull(deletedExpense));

                // Multi-currency expenses
                expenseRepository.save(Objects.requireNonNull(com.intellispend.entity.Expense.builder()
                        .amount(new BigDecimal("45.00"))
                        .category("Travel")
                        .description("Train ticket")
                        .date(LocalDate.now().minusDays(2))
                        .paymentMethod("Cash")
                        .currency("EUR")
                        .user(demoUser)
                        .build()));
            }

            log.info("Comprehensive demo data seeding completed.");
        }
    }
}
