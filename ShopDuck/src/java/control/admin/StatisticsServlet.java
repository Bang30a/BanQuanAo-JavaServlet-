package control.admin;

import dao.DashboardDao;
import service.DashboardService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Lưu ý: Đường dẫn URL mapping phải khớp với đường dẫn bạn redirect trong file JSP
@WebServlet("/admin/StatisticsServlet")
public class StatisticsServlet extends HttpServlet {

    private DashboardService dashboardService;

    @Override
    public void init() throws ServletException {
        // Khởi tạo DAO và Service
        DashboardDao dao = new DashboardDao();
        this.dashboardService = new DashboardService(dao);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        try {
            // Lấy số liệu từ Service
            // Nếu service trả về lỗi hoặc null, ta nên có giá trị mặc định để tránh lỗi JSP
            double totalRevenue = dashboardService.getTotalRevenue();
            int totalOrders = dashboardService.getTotalOrders();
            int totalUsers = dashboardService.getTotalUsers();
            
            List<Map<String, Object>> topSellingProducts = dashboardService.getTopSellingProducts(5);
            if (topSellingProducts == null) {
                topSellingProducts = new ArrayList<>(); // Tránh NullPointerException bên JSP
            }

            // Gửi dữ liệu sang JSP
            request.setAttribute("totalRevenue", totalRevenue);
            request.setAttribute("totalOrders", totalOrders);
            request.setAttribute("totalUsers", totalUsers);
            request.setAttribute("topSellingProducts", topSellingProducts);

            // Forward về trang View
            request.getRequestDispatcher("/admin/Statistics.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            // Nếu có lỗi, vẫn forward về trang nhưng có thể kèm thông báo lỗi
            request.setAttribute("error", "Không thể tải dữ liệu thống kê: " + e.getMessage());
            request.getRequestDispatcher("/admin/Statistics.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}