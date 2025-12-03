package control;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet(name = "LogoutServlet", urlPatterns = {"/LogoutServlet"})
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Lấy session hiện tại (nếu có)
        HttpSession session = request.getSession(false);

        // Hủy session để đăng xuất
        if (session != null) {
            session.invalidate();
        }

        // [SỬA PATH] Redirect về trang Login mới
        response.sendRedirect(request.getContextPath() + "/user/auth/Login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}