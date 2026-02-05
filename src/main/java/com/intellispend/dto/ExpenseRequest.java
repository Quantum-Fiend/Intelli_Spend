package com.intellispend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseRequest {
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotBlank(message = "Category is required")
    @Size(max = 50, message = "Category name is too long")
    private String category;

    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description is too long")
    private String description;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotBlank(message = "Payment method is required")
    @Size(max = 50, message = "Payment method name is too long")
    private String paymentMethod;
    
    @Size(max = 10, message = "Currency code is too long")
    private String currency;
}
