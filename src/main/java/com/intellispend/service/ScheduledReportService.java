package com.intellispend.service;

import com.intellispend.entity.User;
import com.intellispend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledReportService {
    private final ReportService reportService;
    private final UserRepository userRepository;

    // Run on the 1st of every month at 00:00
    @Scheduled(cron = "0 0 0 1 * ?")
    public void generateMonthlyReports() {
        log.info("Starting scheduled monthly report generation...");
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        List<User> users = userRepository.findAll();

        for (User user : users) {
            try {
                // In a real app, we would email this report. 
                // Here we just trigger generation to ensure it's "ready" or cached.
                reportService.generatePdfReport(user.getUsername(), lastMonth);
                log.info("Generated monthly report for user: {}", user.getUsername());
            } catch (Exception e) {
                log.error("Failed to generate scheduled report for user {}: {}", user.getUsername(), e.getMessage());
            }
        }
        log.info("Scheduled monthly report generation completed.");
    }
}
