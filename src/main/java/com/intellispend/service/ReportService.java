package com.intellispend.service;

import com.intellispend.entity.Expense;
import com.intellispend.entity.User;
import com.intellispend.repository.ExpenseRepository;
import com.intellispend.repository.UserRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final InsightService insightService;

    public byte[] generatePdfReport(String username, YearMonth month) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        List<Expense> expenses = expenseRepository.findAllByUser(user).stream()
                .filter(e -> YearMonth.from(e.getDate()).equals(month))
                .collect(Collectors.toList());

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("IntelliSpend - Expense Report")
                    .setFontSize(24)
                    .setBold()
                    .setFontColor(new com.itextpdf.kernel.colors.DeviceRgb(63, 81, 181))); // Indigo color
            
            document.add(new Paragraph("User: " + username).setItalic());
            document.add(new Paragraph("Period: " + month).setItalic());
            document.add(new Paragraph("\n"));

            // Add AI Summary
            com.intellispend.dto.InsightResponse insights = insightService.getMonthlyInsights(username, month);
            document.add(new Paragraph("AI Insights").setBold().setFontSize(16));
            document.add(new Paragraph(insights.getAiSummary()).setItalic().setFontColor(com.itextpdf.kernel.colors.ColorConstants.DARK_GRAY));
            document.add(new Paragraph("\n"));

            // Category Summary Table
            document.add(new Paragraph("Category Breakdown").setBold().setFontSize(16));
            Table categoryTable = new Table(UnitValue.createPercentArray(new float[]{5, 5}));
            categoryTable.setWidth(UnitValue.createPercentValue(60));
            categoryTable.addHeaderCell(new Cell().add(new Paragraph("Category").setBold()));
            categoryTable.addHeaderCell(new Cell().add(new Paragraph("Amount").setBold()));
            
            insights.getCategoryTotals().forEach((cat, total) -> {
                categoryTable.addCell(new Cell().add(new Paragraph(cat)));
                categoryTable.addCell(new Cell().add(new Paragraph(total.toString())));
            });
            document.add(categoryTable);
            document.add(new Paragraph("\n"));

            // Transaction Details
            document.add(new Paragraph("Transaction Details").setBold().setFontSize(16));
            float[] columnDistributions = {3, 4, 6, 4};
            Table table = new Table(UnitValue.createPercentArray(columnDistributions));
            table.setWidth(UnitValue.createPercentValue(100));

            table.addHeaderCell(new Cell().add(new Paragraph("Date").setBold()).setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Category").setBold()).setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Description").setBold()).setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Amount").setBold()).setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY));

            for (Expense expense : expenses) {
                table.addCell(new Cell().add(new Paragraph(expense.getDate().toString())));
                table.addCell(new Cell().add(new Paragraph(expense.getCategory())));
                table.addCell(new Cell().add(new Paragraph(expense.getDescription() != null ? expense.getDescription() : "")));
                table.addCell(new Cell().add(new Paragraph(expense.getAmount().toString() + " " + (expense.getCurrency() != null ? expense.getCurrency() : "USD"))));
            }

            document.add(table);
            document.add(new Paragraph("\nTotal Spending: " + insights.getTotalSpending().toString()).setBold().setFontSize(14));

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF report: " + e.getMessage());
        }
    }

    public byte[] generateCsvReport(String username, YearMonth month) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        List<Expense> expenses = expenseRepository.findAllByUser(user).stream()
                .filter(e -> YearMonth.from(e.getDate()).equals(month))
                .collect(Collectors.toList());

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(out)) {
            
            writer.println("Date,Category,Description,Amount,Payment Method");

            for (Expense expense : expenses) {
                writer.printf("%s,%s,%s,%s,%s%n",
                        expense.getDate(),
                        escapeCsv(expense.getCategory()),
                        escapeCsv(expense.getDescription()),
                        expense.getAmount(),
                        escapeCsv(expense.getPaymentMethod()));
            }

            writer.flush();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate CSV report: " + e.getMessage());
        }
    }

    private String escapeCsv(String data) {
        if (data == null) return "";
        data = data.replace("\"", "\"\"");
        if (data.contains(",") || data.contains("\"") || data.contains("\n")) {
            data = "\"" + data + "\"";
        }
        return data;
    }
}
