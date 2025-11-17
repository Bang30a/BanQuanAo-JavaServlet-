package control.user;

import dao.OrderDao;
import dao.OrderDetailDao;
import dao.ProductDao;
import dao.ProductVariantDao;
import dao.SizeDao;
import entity.OrderDetails;
import entity.Orders;
import entity.ProductVariants;
import entity.Products;
import entity.Size;
import entity.Users;
import service.OrderService; // <-- Dùng lại OrderService

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Đặt servlet tại /user/order-history
@WebServlet(name = "OrderHistoryServlet", urlPatterns = {"/user/order-history"})
public class OrderHistoryServlet extends HttpServlet {

    private OrderService orderService;
    
    // (Chúng ta sẽ cần cập nhật OrderService để nhận thêm DAO)
    // (Xem Bước 2)
    @Override
    public void init() throws ServletException {
        this.orderService = new OrderService(
                new OrderDao(), 
                new OrderDetailDao(), 
                new ProductDao(), 
                new ProductVariantDao(), 
                new SizeDao(), 
                null // DBContext (nếu OrderService của bạn cần)
        );
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        Users user = (Users) session.getAttribute("user");

        // 1. Bắt buộc đăng nhập
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/user/Login.jsp");
            return;
        }

        String action = request.getParameter("action");
        
        try {
            if ("detail".equals(action)) {
                // 2. Hiển thị trang CHI TIẾT
                handleViewDetail(request, response, user);
            } else {
                // 3. Hiển thị trang DANH SÁCH (mặc định)
                handleViewList(request, response, user);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/user/view-products");
        }
    }

    private void handleViewList(HttpServletRequest request, HttpServletResponse response, Users user)
            throws Exception {
        
        // Gọi service để lấy danh sách đơn hàng CỦA USER NÀY
        List<Orders> orderList = orderService.getOrdersForUser(user.getId());
        
        request.setAttribute("orderList", orderList);
        request.getRequestDispatcher("/user/order-history.jsp").forward(request, response);
    }

    private void handleViewDetail(HttpServletRequest request, HttpServletResponse response, Users user)
            throws Exception {
        
        int orderId = Integer.parseInt(request.getParameter("id"));
        
        // 1. Lấy thông tin đơn hàng (phải kiểm tra userId để bảo mật)
        Orders order = orderService.getSecuredOrder(orderId, user.getId());
        
        if (order == null) {
            // Nếu đơn hàng không tồn tại, hoặc không phải của user này -> về trang lịch sử
            response.sendRedirect(request.getContextPath() + "/user/order-history");
            return;
        }
        
        // 2. Lấy chi tiết đơn hàng (bao gồm Tên, Ảnh sản phẩm)
        List<Map<String, Object>> detailsList = orderService.getRichOrderDetails(orderId);
        
        request.setAttribute("order", order);
        request.setAttribute("detailsList", detailsList);
        request.getRequestDispatcher("/user/order-detail.jsp").forward(request, response);
    }
}