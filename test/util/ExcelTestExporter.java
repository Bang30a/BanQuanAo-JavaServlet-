package util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class hỗ trợ xuất kết quả Test ra file Excel (.xlsx) đẹp mắt.
 */
public class ExcelTestExporter {

    // Cấu trúc lưu trữ 1 dòng kết quả
    public static class TestCase {
        String id;
        String scenario;
        String data;
        String steps;
        String expected;
        String actual;
        String status; // PASS hoặc FAIL

        public TestCase(String id, String scenario, String data, String steps, String expected, String actual, String status) {
            this.id = id;
            this.scenario = scenario;
            this.data = data;
            this.steps = steps;
            this.expected = expected;
            this.actual = actual;
            this.status = status;
        }
    }

    // List chứa danh sách kết quả (Static để dùng chung)
    private static final List<TestCase> results = new ArrayList<>();

    // 1. Hàm thêm kết quả (Gọi trong từng @Test)
    public static void addResult(String id, String scenario, String data, String steps, String expected, String actual, String status) {
        results.add(new TestCase(id, scenario, data, steps, expected, actual, status));
    }

    // 2. Hàm xóa kết quả cũ (Gọi trong @BeforeClass nếu cần)
    public static void clearResults() {
        results.clear();
    }

    // 3. Hàm xuất file Excel (Gọi trong @AfterClass)
    public static void exportToExcel(String fileName) {
        System.out.println(">>> Đang xuất file Excel: " + fileName + "...");
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Test Report");

            // --- STYLE ---
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Style PASS (Xanh)
            CellStyle passStyle = workbook.createCellStyle();
            Font passFont = workbook.createFont();
            passFont.setColor(IndexedColors.GREEN.getIndex());
            passFont.setBold(true);
            passStyle.setFont(passFont);

            // Style FAIL (Đỏ)
            CellStyle failStyle = workbook.createCellStyle();
            Font failFont = workbook.createFont();
            failFont.setColor(IndexedColors.RED.getIndex());
            failFont.setBold(true);
            failStyle.setFont(failFont);

            // --- HEADER ---
            String[] columns = {"ID", "Tên Kịch Bản", "Dữ Liệu Mẫu", "Các Bước", "Kết Quả Mong Đợi", "Kết Quả Thực Tế", "Trạng Thái"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // --- DATA ---
            int rowNum = 1;
            for (TestCase tc : results) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(tc.id);
                row.createCell(1).setCellValue(tc.scenario);
                row.createCell(2).setCellValue(tc.data);
                row.createCell(3).setCellValue(tc.steps);
                row.createCell(4).setCellValue(tc.expected);
                row.createCell(5).setCellValue(tc.actual);
                
                Cell statusCell = row.createCell(6);
                statusCell.setCellValue(tc.status);
                
                // Tô màu dựa trên trạng thái
                if ("PASS".equalsIgnoreCase(tc.status)) {
                    statusCell.setCellStyle(passStyle);
                } else {
                    statusCell.setCellStyle(failStyle);
                }
            }

            // Auto size cột cho đẹp
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Lưu file
            try (FileOutputStream fileOut = new FileOutputStream(fileName.replace(".csv", ".xlsx"))) {
                workbook.write(fileOut);
                System.out.println("✅ XUẤT EXCEL THÀNH CÔNG! File: " + fileName.replace(".csv", ".xlsx"));
            }

        } catch (IOException e) {
            System.err.println("❌ Lỗi xuất Excel: " + e.getMessage());
        }
    }
}