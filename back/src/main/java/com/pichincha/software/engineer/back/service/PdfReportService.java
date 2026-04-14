package com.pichincha.software.engineer.back.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.pichincha.software.engineer.back.service.dto.report.AccountReportResponseDto;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;

@Service
public class PdfReportService {

    private static final String[] HEADERS = {"Account Number", "Balance", "Deposits", "Withdrawals"};

    public String generateAccountReportPdf(List<AccountReportResponseDto.Account> accounts) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Title
            document.add(new Paragraph("Account Report")
                    .setFont(bold)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));

            // Table with 4 equal columns, full page width
            Table table = new Table(UnitValue.createPercentArray(new float[]{25, 25, 25, 25}))
                    .useAllAvailableWidth();

            // Header row
            for (String header : HEADERS) {
                table.addHeaderCell(new Cell()
                        .add(new Paragraph(header).setFont(bold))
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setTextAlignment(TextAlignment.CENTER));
            }

            // Data rows
            for (AccountReportResponseDto.Account account : accounts) {
                table.addCell(cell(account.getNumber(), TextAlignment.LEFT, regular));
                table.addCell(cell(formatAmount(account.getBalance()), TextAlignment.RIGHT, regular));
                table.addCell(cell(formatAmount(account.getDeposits()), TextAlignment.RIGHT, regular));
                table.addCell(cell(formatAmount(account.getWithdraws()), TextAlignment.RIGHT, regular));
            }

            document.add(table);
            document.close();

            return Base64.getEncoder().encodeToString(outputStream.toByteArray());

        } catch (IOException e) {
            throw new IllegalStateException("Error generating PDF report", e);
        }
    }

    private Cell cell(String value, TextAlignment alignment, PdfFont font) {
        return new Cell()
                .add(new Paragraph(value != null ? value : "-").setFont(font))
                .setTextAlignment(alignment);
    }

    private String formatAmount(BigDecimal amount) {
        return amount != null ? String.format("%.2f", amount) : "0.00";
    }
}

