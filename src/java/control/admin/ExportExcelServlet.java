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

    private DashboardDao dao;

    // [MỚI] Setter Inject
    public void setDao(DashboardDao dao) {
        this.dao = dao;
    }

    private DashboardDao getDao() {
        if (dao == null) dao = new DashboardDao();
        return dao;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");

        if (startDate == null || startDate.isEmpty()) {
            startDate = LocalDate.now().withDayOfMonth(1).toString();
            endDate = LocalDate.now().toString();
        }

        // [SỬA] Dùng getDao()
        DashboardDao dashboardDao = getDao();
        
        double revenue = dashboardDao.getRevenueByDate(startDate, endDate);
        int orders = dashboardDao.getOrderCountByDate(startDate, endDate);
        List<Map<String, Object>> topProducts = dashboardDao.getTopSellingProducts(10); 

        // 3. Tạo file Excel
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Thống kê");

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            Row row0 = sheet.createRow(0);
            row0.createCell(0).setCellValue("BÁO CÁO THỐNG KÊ DOANH THU");
            
            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("Từ ngày: " + startDate + " - Đến ngày: " + endDate);

            Row row3 = sheet.createRow(3);
            row3.createCell(0).setCellValue("Tổng Doanh Thu");
            row3.createCell(1).setCellValue("Tổng Đơn Hàng");
            row3.getCell(0).setCellStyle(headerStyle);
            row3.getCell(1).setCellStyle(headerStyle);

            Row row4 = sheet.createRow(4);
            row4.createCell(0).setCellValue(revenue);
            row4.createCell(1).setCellValue(orders);

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
                // Check null safety
                String pName = p.get("name") != null ? p.get("name").toString() : "";
                Integer pSold = p.get("total_sold") != null ? (Integer)p.get("total_sold") : 0;
                
                r.createCell(0).setCellValue(pName);
                r.createCell(1).setCellValue(pSold);
            }

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=BaoCao_" + startDate + "_" + endDate + ".xlsx");

            try (OutputStream out = response.getOutputStream()) {
                workbook.write(out);
            }
        }
    }
}