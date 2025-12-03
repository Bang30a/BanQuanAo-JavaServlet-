package control;

import dao.UsersDao;
import entity.Users;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet(name = "RegisterServlet", urlPatterns = {"/RegisterServlet"})
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // [SỬA PATH] Trỏ vào thư mục user/auth/
        response.sendRedirect(request.getContextPath() + "/user/auth/Register.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String fullname = request.getParameter("fullname");
        String email = request.getParameter("email");

        HttpSession session = request.getSession();
        UsersDao dao = new UsersDao(); 

        // 1. Kiểm tra trùng tên đăng nhập
        if (dao.checkUserExists(username)) {
            session.setAttribute("registerError", "⚠️ Tên đăng nhập đã tồn tại!");
            // [SỬA PATH] Quay lại trang đăng ký
            response.sendRedirect(request.getContextPath() + "/user/auth/Register.jsp");
            return;
        }

        // 2. Tạo user mới (Mặc định role là 'user')
        Users newUser = new Users(username, password, fullname, email, "user");
        boolean success = dao.register(newUser);

        if (success) {
            // ✅ Đăng ký thành công
            session.setAttribute("registerSuccess", "🎉 Đăng ký thành công! Vui lòng đăng nhập.");
            // [SỬA PATH] Chuyển sang trang Login
            response.sendRedirect(request.getContextPath() + "/user/auth/Login.jsp");
        } else {
            // ❌ Lỗi database
            session.setAttribute("registerError", "❌ Đăng ký thất bại. Vui lòng thử lại!");
            response.sendRedirect(request.getContextPath() + "/user/auth/Register.jsp");
        }
    }
}