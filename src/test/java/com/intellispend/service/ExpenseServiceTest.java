package com.intellispend.service;

import com.intellispend.entity.Expense;
import com.intellispend.entity.User;
import com.intellispend.repository.ExpenseRepository;
import com.intellispend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BudgetService budgetService;

    @InjectMocks
    private ExpenseService expenseService;

    private User user;
    private Expense expense;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("testuser")
                .build();

        expense = Expense.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .category("Food")
                .date(LocalDate.now())
                .user(user)
                .build();
    }

    @Test
    void testCreateExpense() {
        com.intellispend.dto.ExpenseRequest request = new com.intellispend.dto.ExpenseRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setCategory("Food");
        request.setDate(LocalDate.now());

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);

        com.intellispend.dto.ExpenseResponse response = expenseService.createExpense(request, "testuser");

        assertEquals(new BigDecimal("100.00"), response.getAmount());
        assertEquals("Food", response.getCategory());
        verify(budgetService).checkBudget(any(Expense.class));
    }
}
