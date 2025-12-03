package control.user;

import entity.CartBean;
import entity.Users;
import context.DBContext;
import service.OrderService;
import service.OrderResult;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/user/confirm-checkout")
public class ConfirmCheckoutServlet extends HttpServlet {

    private OrderService orderService;

    @Override
    public void init() throws ServletException {
        DBContext dbContext = new DBContext();
        this.orderService = new OrderService(dbContext); 
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        Users user = (Users) session.getAttribute("user");
        List<CartBean> cart = (List<CartBean>) session.getAttribute("cart");
        String address = request.getParameter("address");
        String phone = request.getParameter("phone");

        OrderResult result = orderService.placeOrder(user, cart, address, phone);
        String contextPath = request.getContextPath();
        
        switch (result) {
            case SUCCESS:
                session.removeAttribute("cart");
                // [SỬA LẠI] Về Servlet trang chủ kèm thông báo
                response.sendRedirect(contextPath + "/user/view-products?success=true");
                break;
                
            case NOT_LOGGED_IN:
                response.sendRedirect(contextPath + "/user/auth/Login.jsp?error=notloggedin");
                break;
                
            case EMPTY_CART:
                response.sendRedirect(contextPath + "/user/order/view-cart.jsp?error=empty");
                break;
                
            case MISSING_INFO:
                request.setAttribute("error", "Vui lòng nhập đầy đủ địa chỉ và số điện thoại!");
                request.getRequestDispatcher("/user/order/Checkout.jsp").forward(request, response);
                break;
                
            case ORDER_FAILED:
            case DETAIL_FAILED:
            case EXCEPTION:
            default:
                request.setAttribute("error", "Đặt hàng thất bại. Mã lỗi: " + result);
                request.getRequestDispatcher("/user/order/Checkout.jsp").forward(request, response);
                break;
        }
    }
}