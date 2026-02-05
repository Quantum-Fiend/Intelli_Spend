package com.intellispend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class CategorizationService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final Map<String, String> KEYWORD_MAP = new HashMap<>();

    static {
        KEYWORD_MAP.put("swiggy", "Food");
        KEYWORD_MAP.put("zomato", "Food");
        KEYWORD_MAP.put("restaurant", "Food");
        KEYWORD_MAP.put("grocery", "Groceries");
        KEYWORD_MAP.put("uber", "Transport");
        KEYWORD_MAP.put("ola", "Transport");
        KEYWORD_MAP.put("petrol", "Transport");
        KEYWORD_MAP.put("amazon", "Shopping");
        KEYWORD_MAP.put("flipkart", "Shopping");
        KEYWORD_MAP.put("netflix", "Entertainment");
        KEYWORD_MAP.put("spotify", "Entertainment");
        KEYWORD_MAP.put("rent", "Housing");
        KEYWORD_MAP.put("electricity", "Utilities");
        KEYWORD_MAP.put("water", "Utilities");
        KEYWORD_MAP.put("internet", "Utilities");
    }

    public String categorize(String description) {
        if (description == null || description.isEmpty()) {
            return "Other";
        }

        String lowerDesc = description.toLowerCase();

        // 1. Keyword-based fallback
        for (Map.Entry<String, String> entry : KEYWORD_MAP.entrySet()) {
            if (lowerDesc.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // 2. OpenAI-based categorization (if API key is present)
        if (apiKey != null && !apiKey.isEmpty() && !apiKey.equals("your_openai_api_key_here")) {
            try {
                return categorizeWithAi(description);
            } catch (Exception e) {
                log.error("AI Categorization failed: {}", e.getMessage());
            }
        }

        return "Other";
    }

    @org.springframework.retry.annotation.Retryable(
            retryFor = {Exception.class},
            maxAttempts = 3,
            backoff = @org.springframework.retry.annotation.Backoff(delay = 1000, multiplier = 2)
    )
    private String categorizeWithAi(String description) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", com.intellispend.util.PromptConstants.CATEGORIZATION_SYSTEM_PROMPT);
        
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", String.format(com.intellispend.util.PromptConstants.CATEGORIZATION_USER_PROMPT_TEMPLATE, description));
        
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

        return "Other";
    }
}
