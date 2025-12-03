package control.admin;

import dao.OrderDao;
import dao.OrderDetailDao;
import dao.ProductVariantDao;
import entity.OrderDetails;
import service.OrderAdminService; // <-- Import service

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/admin/OrderDetailManagerServlet")
public class OrderDetailManagerServlet extends HttpServlet {

    private OrderAdminService adminService; // <-- Tham chiếu đến service

    @Override
    public void init() throws ServletException {
        // Khởi tạo service (cần cả 3 DAO, giống như OrdersManagerServlet)
        OrderDao orderDao = new OrderDao();
        OrderDetailDao detailDao = new OrderDetailDao();
        ProductVariantDao variantDao = new ProductVariantDao();
        
        this.adminService = new OrderAdminService(orderDao, detailDao, variantDao);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        // KHÔNG tạo DAO ở đây nữa

        try {
            int orderId = parseInt(request.getParameter("orderId"));
            
            // Gọi Service để lấy dữ liệu (an toàn, trả về list rỗng nếu lỗi)
            List<OrderDetails> details = adminService.getDetailsForOrder(orderId);

            request.setAttribute("ORDER_ID", orderId);
            request.setAttribute("DETAILS", details);
            request.getRequestDispatcher("/admin/order/View-order-detail.jsp").forward(request, response);            
        } catch (Exception e) { // Bắt các lỗi khác, ví dụ: NumberFormatException
            e.printStackTrace();
            request.setAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    private int parseInt(String value) {
        return (value != null && !value.isEmpty()) ? Integer.parseInt(value) : 0;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}