package control.admin;

import dao.DashboardDao;
import service.DashboardService; // <-- Import service

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/admin/StatisticsServlet")
public class StatisticsServlet extends HttpServlet {

    private DashboardService dashboardService; // <-- Tham chiếu đến service

    @Override
    public void init() throws ServletException {
        // Khởi tạo DAO và "tiêm" nó vào Service
        DashboardDao dao = new DashboardDao();
        this.dashboardService = new DashboardService(dao);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // --- Gọi Service thay vì DAO ---
        // Các hàm này giờ đã an toàn, không ném Exception
        double totalRevenue = dashboardService.getTotalRevenue();
        int totalOrders = dashboardService.getTotalOrders();
        int totalUsers = dashboardService.getTotalUsers();
        List<Map<String, Object>> topSellingProducts = dashboardService.getTopSellingProducts(5);
        // --- Hết phần gọi Service ---

        request.setAttribute("totalRevenue", totalRevenue);
        request.setAttribute("totalOrders", totalOrders);
        request.setAttribute("totalUsers", totalUsers);
        request.setAttribute("topSellingProducts", topSellingProducts);

        request.getRequestDispatcher("/admin/Statistics.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}