package com.intellispend.controller;

import com.intellispend.dto.ExpenseRequest;
import com.intellispend.dto.ExpenseResponse;
import com.intellispend.dto.MessageResponse;
import com.intellispend.service.ExpenseService;
import jakarta.validation.Valid;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
public class ExpenseController {
    private final ExpenseService expenseService;
    private final com.intellispend.service.CsvService csvService;

    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(@Valid @RequestBody ExpenseRequest expenseRequest, Authentication authentication) {
        return ResponseEntity.ok(expenseService.createExpense(expenseRequest, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getAllExpenses(Authentication authentication) {
        return ResponseEntity.ok(expenseService.getAllExpenses(authentication.getName()));
    }

    @GetMapping("/paginated")
    public ResponseEntity<org.springframework.data.domain.Page<ExpenseResponse>> getPaginatedExpenses(
            @org.springframework.data.web.PageableDefault(size = 10, sort = "date", direction = org.springframework.data.domain.Sort.Direction.DESC) org.springframework.data.domain.Pageable pageable,
            Authentication authentication) {
        return ResponseEntity.ok(expenseService.getPaginatedExpenses(authentication.getName(), pageable));
    }

    @GetMapping("/filter")
    public ResponseEntity<org.springframework.data.domain.Page<ExpenseResponse>> getFilteredExpenses(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate,
            @RequestParam(required = false) java.math.BigDecimal minAmount,
            @RequestParam(required = false) java.math.BigDecimal maxAmount,
            @RequestParam(required = false) String description,
            @org.springframework.data.web.PageableDefault(size = 10, sort = "date", direction = org.springframework.data.domain.Sort.Direction.DESC) org.springframework.data.domain.Pageable pageable,
            Authentication authentication) {
        return ResponseEntity.ok(expenseService.getFilteredExpenses(
                authentication.getName(), category, startDate, endDate, minAmount, maxAmount, description, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> updateExpense(@PathVariable Long id, @Valid @RequestBody ExpenseRequest expenseRequest, Authentication authentication) {
        return ResponseEntity.ok(expenseService.updateExpense(id, expenseRequest, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteExpense(@PathVariable Long id, Authentication authentication) {
        expenseService.deleteExpense(id, authentication.getName());
        return ResponseEntity.ok(new MessageResponse("Expense deleted successfully"));
    }

    @PostMapping("/upload")
    public ResponseEntity<List<ExpenseResponse>> uploadExpenses(@RequestParam("file") org.springframework.web.multipart.MultipartFile file, Authentication authentication) {
        List<ExpenseRequest> requests = csvService.parseCsv(file);
        return ResponseEntity.ok(expenseService.createExpenses(requests, authentication.getName()));
    }
}
