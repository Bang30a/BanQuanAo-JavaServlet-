package control;

import dao.UsersDao;
import entity.Users;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/user/Register.jsp");
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
        UsersDao dao = createDao();

        if (dao.checkUserExists(username)) {
            // ❌ Tên đăng nhập trùng
            session.setAttribute("registerError", "⚠️ Tên đăng nhập đã tồn tại!");
            response.sendRedirect(request.getContextPath() + "/user/Register.jsp");
            return;
        }

        Users newUser = new Users(username, password, fullname, email, "user");
        boolean success = dao.register(newUser);

        if (success) {
            // ✅ Đăng ký thành công
            session.setAttribute("registerSuccess", "🎉 Đăng ký thành công! Vui lòng đăng nhập.");
            response.sendRedirect(request.getContextPath() + "/user/Login.jsp");
        } else {
            // ❌ Lỗi không xác định
            session.setAttribute("registerError", "❌ Đăng ký thất bại. Vui lòng thử lại!");
            response.sendRedirect(request.getContextPath() + "/user/Register.jsp");
        }
    }

    protected UsersDao createDao() {
        return new UsersDao();
    }

    @Override
    public String getServletInfo() {
        return "Xử lý đăng ký người dùng mới.";
    }
}
