package control;

import dao.UsersDao;
import entity.Users;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Chuyển hướng đến trang login
        response.sendRedirect("Login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        UsersDao dao = new UsersDao();
        Users user = dao.login(username, password);

        if (user == null) {
            request.setAttribute("error", "❌ Sai tên đăng nhập hoặc mật khẩu!");
            request.getRequestDispatcher("Login.jsp").forward(request, response);
        } else {
            // Tạo session mới
            HttpSession session = request.getSession(true);
            session.setAttribute("user", user);

            // Chuyển hướng theo vai trò
            String role = user.getRole().toLowerCase();
            switch (role) {
                case "admin":
                    response.sendRedirect(request.getContextPath() + "/admin/Dashboard.jsp");
                    break;
                case "user":
                    response.sendRedirect(request.getContextPath() + "/user/View-products.jsp");
                    break;
                default:
                    // Nếu có role lạ
                    session.invalidate(); // Hủy session nếu không hợp lệ
                    request.setAttribute("error", "Quyền truy cập không hợp lệ.");
                    request.getRequestDispatcher("Login.jsp").forward(request, response);
                    break;
            }
        }
    }

    @Override
    public String getServletInfo() {
        return "Xử lý đăng nhập người dùng và phân quyền truy cập.";
    }
}
