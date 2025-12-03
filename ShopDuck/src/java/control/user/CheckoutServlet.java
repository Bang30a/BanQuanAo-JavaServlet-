package control.user;

import entity.Users;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/user/checkout")
public class CheckoutServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 1. Kiểm tra đăng nhập (Bảo mật)
        HttpSession session = request.getSession();
        Users user = (Users) session.getAttribute("user");

        if (user == null) {
            // Lưu lại trang hiện tại để login xong quay lại đúng chỗ này
            session.setAttribute("redirectAfterLogin", request.getRequestURI());
            session.setAttribute("loginError", "Vui lòng đăng nhập để tiến hành thanh toán!");
            
            // [SỬA ĐƯỜNG DẪN] Chuyển hướng về trang Login mới
            response.sendRedirect(request.getContextPath() + "/user/auth/Login.jsp");
            return;
        }

        // 2. Kiểm tra giỏ hàng trống (Tuỳ chọn)
        if (session.getAttribute("cart") == null || ((java.util.List)session.getAttribute("cart")).isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/user/order/view-cart.jsp");
            return;
        }

        // 3. [SỬA ĐƯỜNG DẪN] Forward sang file JSP ở vị trí mới
        request.getRequestDispatcher("/user/order/Checkout.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}