package com.intellispend.service;

import com.intellispend.dto.InsightResponse;
import com.intellispend.entity.Expense;
import com.intellispend.entity.User;
import com.intellispend.repository.ExpenseRepository;
import com.intellispend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InsightService {
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final com.intellispend.repository.InsightRepository insightRepository;

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public InsightResponse getMonthlyInsights(String username, YearMonth month) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // 1. Check for persisted insight
        java.util.Optional<com.intellispend.entity.Insight> persistedInsight = insightRepository.findByUserAndMonth(user, month);
        
        List<Expense> expenses = expenseRepository.findAllByUser(user);
        
        // ... (rest of the logic)

        // Filter by month
        List<Expense> currentMonthExpenses = expenses.stream()
                .filter(e -> YearMonth.from(e.getDate()).equals(month))
                .collect(Collectors.toList());

        List<Expense> previousMonthExpenses = expenses.stream()
                .filter(e -> YearMonth.from(e.getDate()).equals(month.minusMonths(1)))
                .collect(Collectors.toList());

        // Category breakdown
        Map<String, BigDecimal> categoryTotals = currentMonthExpenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));

        BigDecimal currentTotal = currentMonthExpenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal previousTotal = previousMonthExpenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Double momPercentage = 0.0;
        if (previousTotal.compareTo(BigDecimal.ZERO) > 0) {
            momPercentage = currentTotal.subtract(previousTotal)
                    .divide(previousTotal, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100))
                    .doubleValue();
        }

        String aiSummary = "AI Summary not available.";
        if (persistedInsight.isPresent()) {
            aiSummary = persistedInsight.get().getSummary();
        } else if (apiKey != null && !apiKey.isEmpty() && !apiKey.equals("your_openai_api_key_here")) {
            aiSummary = generateAiSummary(categoryTotals, currentTotal, previousTotal, momPercentage);
            insightRepository.save(java.util.Objects.requireNonNull(com.intellispend.entity.Insight.builder()
                    .user(user)
                    .month(month)
                    .summary(aiSummary)
                    .build()));
        }

        // Daily spending
        java.util.Map<LocalDate, BigDecimal> dailySpending = currentMonthExpenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getDate,
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));

        // Weekly spending (Week of Month)
        java.util.Map<Integer, BigDecimal> weeklySpending = currentMonthExpenses.stream()
                .collect(Collectors.groupingBy(
                        e -> (e.getDate().getDayOfMonth() - 1) / 7 + 1,
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));

        return InsightResponse.builder()
                .categoryTotals(categoryTotals)
                .totalSpending(currentTotal)
                .previousMonthTotal(previousTotal)
                .monthOverMonthPercentage(momPercentage)
                .aiSummary(aiSummary)
                .dailySpending(dailySpending)
                .weeklySpending(weeklySpending)
                .build();
    }

    @org.springframework.retry.annotation.Retryable(
            retryFor = {Exception.class},
            maxAttempts = 3,
            backoff = @org.springframework.retry.annotation.Backoff(delay = 2000, multiplier = 2)
    )
    private String generateAiSummary(Map<String, BigDecimal> categoryTotals, BigDecimal currentTotal, BigDecimal previousTotal, Double momPercentage) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            String prompt = String.format(
                    com.intellispend.util.PromptConstants.INSIGHT_USER_PROMPT_TEMPLATE,
                    currentTotal, previousTotal, momPercentage, categoryTotals.toString()
            );

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-3.5-turbo");
            
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", com.intellispend.util.PromptConstants.INSIGHT_SYSTEM_PROMPT);
            
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            
            requestBody.put("messages", new Object[]{systemMessage, userMessage});

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<com.intellispend.dto.external.OpenAiResponse> response = restTemplate.postForEntity(apiUrl, entity, com.intellispend.dto.external.OpenAiResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                com.intellispend.dto.external.OpenAiResponse aiResponse = response.getBody();
                if (aiResponse != null && aiResponse.getChoices() != null && !aiResponse.getChoices().isEmpty()) {
                    com.intellispend.dto.external.OpenAiResponse.Message message = aiResponse.getChoices().get(0).getMessage();
                    if (message != null && message.getContent() != null) {
                        return message.getContent().trim();
                    }
                }
            }
        } catch (Exception e) {
            log.error("AI Summary generation failed: {}", e.getMessage());
        }
        return "Insight generation failed or took too long.";
    }
}
