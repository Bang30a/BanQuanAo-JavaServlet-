package control.admin;

import dao.DashboardDao;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/admin/dashboard")
public class DashboardServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        DashboardDao dao = new DashboardDao();

        // 1. Lấy số liệu tổng quan (Cards)
        double totalRevenue = dao.getTotalRevenue();
        int totalOrders = dao.getTotalOrders();
        int totalUsers = dao.getTotalUsers();

        // 2. Lấy danh sách Top sản phẩm
        List<Map<String, Object>> topProducts = dao.getTopSellingProducts(5);

        // 3. Lấy dữ liệu biểu đồ (MỚI)
        List<Map<String, Object>> revenueChartData = dao.getRevenueLast7Days();
        List<Map<String, Object>> statusChartData = dao.getOrderStatusStats();

        // 4. Đẩy ra JSP
        request.setAttribute("totalRevenue", totalRevenue);
        request.setAttribute("totalOrders", totalOrders);
        request.setAttribute("totalUsers", totalUsers);
        request.setAttribute("topSellingProducts", topProducts);
        
        request.setAttribute("revenueChartData", revenueChartData);
        request.setAttribute("statusChartData", statusChartData);

        request.getRequestDispatcher("/admin/dashboard/Dashboard.jsp").forward(request, response);
    }
}