package control.user;

import context.DBContext;
import entity.Orders;
import entity.Users;
import service.OrderService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "OrderHistoryServlet", urlPatterns = {"/user/order-history"})
public class OrderHistoryServlet extends HttpServlet {

    private OrderService orderService;

    @Override
    public void init() throws ServletException {
        DBContext dbContext = new DBContext();
        this.orderService = new OrderService(dbContext);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        Users user = (Users) session.getAttribute("user");

        if (user == null) {
            session.setAttribute("redirectAfterLogin", request.getRequestURI() + (request.getQueryString() != null ? "?" + request.getQueryString() : ""));
            session.setAttribute("loginError", "Vui lòng đăng nhập để xem lịch sử đơn hàng.");
            response.sendRedirect(request.getContextPath() + "/user/auth/Login.jsp");
            return;
        }

        String action = request.getParameter("action");
        
        try {
            if ("detail".equals(action)) {
                handleViewDetail(request, response, user);
            } else {
                handleViewList(request, response, user);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // [SỬA LẠI] Về trang chủ Servlet nếu lỗi
            response.sendRedirect(request.getContextPath() + "/user/view-products");
        }
    }

    private void handleViewList(HttpServletRequest request, HttpServletResponse response, Users user) throws Exception {
        List<Orders> orderList = orderService.getOrdersForUser(user.getId());
        request.setAttribute("orderList", orderList);
        request.getRequestDispatcher("/user/order/order-history.jsp").forward(request, response);
    }

    private void handleViewDetail(HttpServletRequest request, HttpServletResponse response, Users user) throws Exception {
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/user/order-history");
            return;
        }

        int orderId = Integer.parseInt(idParam);
        Orders order = orderService.getSecuredOrder(orderId, user.getId());
        
        if (order == null) {
            response.sendRedirect(request.getContextPath() + "/user/order-history");
            return;
        }
        
        List<Map<String, Object>> detailsList = orderService.getRichOrderDetails(orderId);
        request.setAttribute("order", order);
        request.setAttribute("detailsList", detailsList);
        request.getRequestDispatcher("/user/order/order-detail.jsp").forward(request, response);
    }
}