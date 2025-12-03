package control.admin;

import dao.DashboardDao;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/admin/statistics")
public class StatisticsServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");

        // Mặc định lấy tháng hiện tại nếu chưa chọn
        if (startDate == null) {
            LocalDate today = LocalDate.now();
            startDate = today.withDayOfMonth(1).toString();
            endDate = today.toString();
        }

        DashboardDao dao = new DashboardDao();
        
        // 1. Số liệu tổng quan trong khoảng thời gian
        double revenue = dao.getRevenueByDate(startDate, endDate);
        int orders = dao.getOrderCountByDate(startDate, endDate);
        
        // 2. BẢNG DỮ LIỆU chi tiết (Top sản phẩm bán trong khoảng thời gian này)
        List<Map<String, Object>> reportData = dao.getTopSellingProductsByDate(startDate, endDate);

        // 3. Đẩy ra JSP
        request.setAttribute("revenue", revenue);
        request.setAttribute("orders", orders);
        request.setAttribute("reportData", reportData); // Dữ liệu cho bảng
        
        request.setAttribute("start", startDate);
        request.setAttribute("end", endDate);

        request.getRequestDispatcher("/admin/dashboard/Statistics.jsp").forward(request, response);
    }
}