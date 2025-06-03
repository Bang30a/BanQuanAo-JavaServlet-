package control;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Lấy session nếu tồn tại, không tạo mới
        HttpSession session = request.getSession(false);

        // Hủy session nếu có
        if (session != null) {
            session.invalidate();
        }

        // Redirect về trang login
        response.sendRedirect(request.getContextPath() + "/user/Login.jsp");
    }

    @Override
    public String getServletInfo() {
        return "Xử lý đăng xuất người dùng";
    }
}
