package com.intellispend.service;

import com.intellispend.dto.ExpenseRequest;
import com.intellispend.dto.ExpenseResponse;
import com.intellispend.entity.Expense;
import com.intellispend.entity.User;
import com.intellispend.repository.ExpenseRepository;
import com.intellispend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final CategorizationService categorizationService;
    private final BudgetService budgetService;

    @Transactional
    public ExpenseResponse createExpense(ExpenseRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        String category = request.getCategory();
        if (category == null || category.trim().isEmpty() || category.equalsIgnoreCase("Other")) {
            category = categorizationService.categorize(request.getDescription());
        }

        Expense expense = Expense.builder()
                .amount(request.getAmount())
                .category(category)
                .description(request.getDescription())
                .date(request.getDate())
                .paymentMethod(request.getPaymentMethod())
                .currency(request.getCurrency())
                .user(user)
                .build();

        Expense savedExpense = expenseRepository.save(expense);
        budgetService.checkBudget(savedExpense);
        return mapToResponse(savedExpense);
    }

    @Transactional
    public List<ExpenseResponse> createExpenses(List<ExpenseRequest> requests, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        List<Expense> expenses = requests.stream()
                .map(request -> {
                    String category = request.getCategory();
                    if (category == null || category.trim().isEmpty() || category.equalsIgnoreCase("Other")) {
                        category = categorizationService.categorize(request.getDescription());
                    }
                    return Expense.builder()
                            .amount(request.getAmount())
                            .category(category)
                            .description(request.getDescription())
                            .date(request.getDate())
                            .paymentMethod(request.getPaymentMethod())
                            .currency(request.getCurrency())
                            .user(user)
                            .build();
                })
                .collect(Collectors.toList());

        return expenseRepository.saveAll(expenses).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ExpenseResponse> getAllExpenses(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return expenseRepository.findAllByUser(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public org.springframework.data.domain.Page<ExpenseResponse> getPaginatedExpenses(String username, org.springframework.data.domain.Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return expenseRepository.findAllByUser(user, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public ExpenseResponse updateExpense(Long id, ExpenseRequest request, String username) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));

        if (!expense.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized to update this expense");
        }

        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setDescription(request.getDescription());
        expense.setDate(request.getDate());
        expense.setPaymentMethod(request.getPaymentMethod());

        Expense updatedExpense = expenseRepository.save(expense);
        return mapToResponse(updatedExpense);
    }

    @Transactional
    public void deleteExpense(Long id, String username) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));

        if (!expense.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized to delete this expense");
        }

        expenseRepository.delete(expense);
    }

    public org.springframework.data.domain.Page<ExpenseResponse> getFilteredExpenses(
            String username,
            String category,
            java.time.LocalDate startDate,
            java.time.LocalDate endDate,
            java.math.BigDecimal minAmount,
            java.math.BigDecimal maxAmount,
            String description,
            org.springframework.data.domain.Pageable pageable) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        org.springframework.data.jpa.domain.Specification<Expense> spec = com.intellispend.repository.ExpenseSpecification.filterBy(
                        user, category, startDate, endDate, minAmount, maxAmount, description);

        return expenseRepository.findAll(java.util.Objects.requireNonNull(spec), 
                java.util.Objects.requireNonNull(pageable))
                .map(this::mapToResponse);
    }

    private ExpenseResponse mapToResponse(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .amount(expense.getAmount())
                .category(expense.getCategory())
                .description(expense.getDescription())
                .date(expense.getDate())
                .paymentMethod(expense.getPaymentMethod())
                .currency(expense.getCurrency())
                .build();
    }
}
