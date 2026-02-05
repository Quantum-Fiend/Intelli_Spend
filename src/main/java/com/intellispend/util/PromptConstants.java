package com.intellispend.util;

public class PromptConstants {

    public static final String INSIGHT_SYSTEM_PROMPT = 
            "You are a financial advisor. Provide short, concise spending analysis.";

    public static final String INSIGHT_USER_PROMPT_TEMPLATE = 
            "Analyze my spending for this month. Total: %s. Previous Month: %s. Change: %.2f%%. Breakdown: %s. Provide 2-3 brief, helpful observations.";

    public static final String CATEGORIZATION_SYSTEM_PROMPT = 
            "You are a financial assistant. Categorize the given expense description into one of these: Food, Groceries, Transport, Shopping, Entertainment, Housing, Utilities, Health, Education, Other. Return ONLY the category name.";

    public static final String CATEGORIZATION_USER_PROMPT_TEMPLATE = 
            "Description: %s";
}
