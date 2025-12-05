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

    private DashboardDao dao;

    // [MỚI] Setter để Inject Mock DAO
    public void setDao(DashboardDao dao) {
        this.dao = dao;
    }

    // [MỚI] Getter lazy load
    private DashboardDao getDao() {
        if (dao == null) {
            dao = new DashboardDao();
        }
        return dao;
    }

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

        // [SỬA] Dùng getDao()
        DashboardDao dashboardDao = getDao();
        
        // 1. Số liệu tổng quan trong khoảng thời gian
        double revenue = dashboardDao.getRevenueByDate(startDate, endDate);
        int orders = dashboardDao.getOrderCountByDate(startDate, endDate);
        
        // 2. BẢNG DỮ LIỆU chi tiết
        List<Map<String, Object>> reportData = dashboardDao.getTopSellingProductsByDate(startDate, endDate);

        // 3. Đẩy ra JSP
        request.setAttribute("revenue", revenue);
        request.setAttribute("orders", orders);
        request.setAttribute("reportData", reportData);
        
        request.setAttribute("start", startDate);
        request.setAttribute("end", endDate);

        request.getRequestDispatcher("/admin/dashboard/Statistics.jsp").forward(request, response);
    }
}