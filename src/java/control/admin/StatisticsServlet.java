package control.admin;

import dao.DashboardDao;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/admin/StatisticsServlet")
public class StatisticsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        DashboardDao dao = new DashboardDao();

        double totalRevenue = dao.getTotalRevenue();
        int totalOrders = dao.getTotalOrders();
        int totalUsers = dao.getTotalUsers();
        List<Map<String, Object>> topSellingProducts = dao.getTopSellingProducts(5);

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
