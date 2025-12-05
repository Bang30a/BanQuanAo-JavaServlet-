package control;

import dao.UsersDao;
import entity.Users;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet(name = "RegisterServlet", urlPatterns = {"/RegisterServlet"})
public class RegisterServlet extends HttpServlet {

    private UsersDao usersDao;

    // Hàm Setter cho Mock Test
    public void setUsersDao(UsersDao usersDao) {
        this.usersDao = usersDao;
    }

    // Hàm Getter lấy DAO
    private UsersDao getUsersDao() {
        if (usersDao == null) {
            usersDao = new UsersDao();
        }
        return usersDao;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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
        
        // 0. [THÊM MỚI] Kiểm tra độ mạnh mật khẩu
        // Điều kiện: Không được để trống và phải có ít nhất 6 ký tự
        if (password == null || password.trim().length() < 6) {
            session.setAttribute("registerError", "Mật khẩu quá yếu! Vui lòng nhập ít nhất 6 ký tự.");
            response.sendRedirect(request.getContextPath() + "/user/auth/Register.jsp");
            return; // Dừng xử lý, không lưu vào DB
        }

        UsersDao dao = getUsersDao(); 

        // 1. Kiểm tra trùng tên đăng nhập
        if (dao.checkUserExists(username)) {
            session.setAttribute("registerError", "Tên đăng nhập đã tồn tại!");
            response.sendRedirect(request.getContextPath() + "/user/auth/Register.jsp");
            return;
        }

        // 2. Tạo user mới
        Users newUser = new Users(username, password, fullname, email, "user");
        boolean success = dao.register(newUser);

        if (success) {
            // ✅ Đăng ký thành công -> Lưu thông báo -> Chuyển sang Login
            session.setAttribute("registerSuccess", "Đăng ký thành công! Vui lòng đăng nhập.");
            response.sendRedirect(request.getContextPath() + "/user/auth/Login.jsp");
        } else {
            // ❌ Lỗi database
            session.setAttribute("registerError", "Đăng ký thất bại. Vui lòng thử lại!");
            response.sendRedirect(request.getContextPath() + "/user/auth/Register.jsp");
        }
    }
}