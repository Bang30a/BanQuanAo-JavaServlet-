package control.user;

// CHỈ CẦN IMPORT NHỮNG GÓI NÀY
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

    private OrderService orderService; // <-- Tham chiếu đến Service

    @Override
    public void init() throws ServletException {
        
        // Khởi tạo DBContext
        DBContext dbContext = new DBContext();
        
        // Gọi đúng constructor (hàm khởi tạo) chỉ có 1 tham số.
        this.orderService = new OrderService(dbContext); 
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        // --- 1. Lấy dữ liệu ---
        HttpSession session = request.getSession();
        Users user = (Users) session.getAttribute("user");
        List<CartBean> cart = (List<CartBean>) session.getAttribute("cart");
        String address = request.getParameter("address");
        String phone = request.getParameter("phone");

        // --- 2. Gọi Service ---
        OrderResult result = orderService.placeOrder(user, cart, address, phone);

        // --- 3. Điều hướng ---
        String contextPath = request.getContextPath();
        switch (result) {
            case SUCCESS:
                session.removeAttribute("cart"); // Xóa giỏ hàng
                
                // --- SỬA LỖI Ở ĐÂY ---
                // Chuyển hướng đến Servlet (bỏ .jsp) để nó tải sản phẩm lên
                response.sendRedirect(contextPath + "/user/view-products?success=true");
                break; // <-- Thêm break
                
            case NOT_LOGGED_IN:
                response.sendRedirect(contextPath + "/user/Login.jsp?error=notloggedin");
                break;
            case EMPTY_CART:
                response.sendRedirect(contextPath + "/user/view-cart.jsp?error=empty");
                break;
            case MISSING_INFO:
                // Sửa lại URL trang checkout cho đúng (dựa trên ảnh đầu tiên của bạn)
                response.sendRedirect(contextPath + "/user/checkout.jsp?error=missinginfo");
                break;
            case ORDER_FAILED:
                response.sendRedirect(contextPath + "/user/checkout.jsp?error=orderfail");
                break;
            case DETAIL_FAILED:
                response.sendRedirect(contextPath + "/user/checkout.jsp?error=orderdetailfail");
                break;
            case EXCEPTION:
            default:
                response.sendRedirect(contextPath + "/user/checkout.jsp?error=exception");
                break;
        }
    }
}