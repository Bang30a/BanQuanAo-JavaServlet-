package control.admin;

import dao.DashboardDao;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/admin/dashboard")
public class DashboardServlet extends HttpServlet {

    private DashboardDao dao;

    // [MỚI] Setter để Unit Test có thể inject Mock DAO
    public void setDao(DashboardDao dao) {
        this.dao = dao;
    }

    // [MỚI] Getter lazy load (để chạy thật vẫn hoạt động bình thường)
    private DashboardDao getDao() {
        if (dao == null) {
            dao = new DashboardDao();
        }
        return dao;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            // [SỬA] Dùng getDao() thay vì new trực tiếp
            DashboardDao dashboardDao = getDao();

            // 1. Lấy số liệu tổng quan (Cards)
            double totalRevenue = dashboardDao.getTotalRevenue();
            int totalOrders = dashboardDao.getTotalOrders();
            int totalUsers = dashboardDao.getTotalUsers();

            // 2. Lấy danh sách Top sản phẩm
            List<Map<String, Object>> topProducts = dashboardDao.getTopSellingProducts(5);

            // 3. Lấy dữ liệu biểu đồ
            List<Map<String, Object>> revenueChartData = dashboardDao.getRevenueLast7Days();
            List<Map<String, Object>> statusChartData = dashboardDao.getOrderStatusStats();

            // 4. Đẩy ra JSP
            request.setAttribute("totalRevenue", totalRevenue);
            request.setAttribute("totalOrders", totalOrders);
            request.setAttribute("totalUsers", totalUsers);
            request.setAttribute("topSellingProducts", topProducts);
            
            request.setAttribute("revenueChartData", revenueChartData);
            request.setAttribute("statusChartData", statusChartData);

            request.getRequestDispatcher("/admin/dashboard/Dashboard.jsp").forward(request, response);

        } catch (Exception e) {
            // [MỚI] Bắt lỗi để không sập trang web (Error Handling)
            Logger.getLogger(DashboardServlet.class.getName()).log(Level.SEVERE, "Dashboard Error", e);
            // Vẫn forward về trang dashboard (có thể hiện dữ liệu trống hoặc thông báo lỗi)
            request.setAttribute("error", "Hệ thống đang bận, vui lòng thử lại sau.");
            request.getRequestDispatcher("/admin/dashboard/Dashboard.jsp").forward(request, response);
        }
    }
}