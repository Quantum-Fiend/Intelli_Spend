package com.intellispend.service;

import com.intellispend.dto.ExpenseRequest;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CsvService {

    public List<ExpenseRequest> parseCsv(MultipartFile file) {
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CsvToBean<CsvExpense> csvToBean = new CsvToBeanBuilder<CsvExpense>(reader)
                    .withType(CsvExpense.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            return csvToBean.parse().stream()
                    .map(csv -> {
                        ExpenseRequest req = new ExpenseRequest();
                        req.setAmount(new BigDecimal(csv.getAmount()));
                        req.setCategory(csv.getCategory());
                        req.setDescription(csv.getDescription());
                        req.setDate(LocalDate.parse(csv.getDate()));
                        req.setPaymentMethod(csv.getPaymentMethod());
                        return req;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage());
        }
    }

    public static class CsvExpense {
        private String amount;
        private String category;
        private String description;
        private String date;
        private String paymentMethod;

        public String getAmount() { return amount; }
        public void setAmount(String amount) { this.amount = amount; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    }
}
