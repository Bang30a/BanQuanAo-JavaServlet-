package control.admin;

import dao.DashboardDao;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@WebServlet("/admin/export-excel")
public class ExportExcelServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Lấy ngày từ request (hoặc mặc định là tháng hiện tại)
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");

        if (startDate == null || startDate.isEmpty()) {
            startDate = LocalDate.now().withDayOfMonth(1).toString();
            endDate = LocalDate.now().toString();
        }

        // 2. Lấy dữ liệu từ DAO
        DashboardDao dao = new DashboardDao();
        double revenue = dao.getRevenueByDate(startDate, endDate);
        int orders = dao.getOrderCountByDate(startDate, endDate);
        // Lấy top sản phẩm (bạn có thể viết thêm hàm getTopSellingProductsByDate nếu cần lọc kỹ hơn)
        List<Map<String, Object>> topProducts = dao.getTopSellingProducts(10); 

        // 3. Tạo file Excel
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Thống kê");

            // Style cho header
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // Dòng tiêu đề
            Row row0 = sheet.createRow(0);
            row0.createCell(0).setCellValue("BÁO CÁO THỐNG KÊ DOANH THU");
            
            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("Từ ngày: " + startDate + " - Đến ngày: " + endDate);

            // Dữ liệu tổng quan
            Row row3 = sheet.createRow(3);
            row3.createCell(0).setCellValue("Tổng Doanh Thu");
            row3.createCell(1).setCellValue("Tổng Đơn Hàng");
            row3.getCell(0).setCellStyle(headerStyle);
            row3.getCell(1).setCellStyle(headerStyle);

            Row row4 = sheet.createRow(4);
            row4.createCell(0).setCellValue(revenue);
            row4.createCell(1).setCellValue(orders);

            // Dữ liệu Top sản phẩm
            Row row6 = sheet.createRow(6);
            row6.createCell(0).setCellValue("TOP SẢN PHẨM BÁN CHẠY");
            row6.getCell(0).setCellStyle(headerStyle);

            Row row7 = sheet.createRow(7);
            row7.createCell(0).setCellValue("Tên sản phẩm");
            row7.createCell(1).setCellValue("Số lượng bán");
            row7.getCell(0).setCellStyle(headerStyle);
            row7.getCell(1).setCellStyle(headerStyle);

            int rowNum = 8;
            for (Map<String, Object> p : topProducts) {
                Row r = sheet.createRow(rowNum++);
                r.createCell(0).setCellValue((String) p.get("name"));
                r.createCell(1).setCellValue((Integer) p.get("total_sold"));
            }

            // Auto size cột
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            // 4. Trả file về trình duyệt
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=BaoCao_" + startDate + "_" + endDate + ".xlsx");

            try (OutputStream out = response.getOutputStream()) {
                workbook.write(out);
            }
        }
    }
}