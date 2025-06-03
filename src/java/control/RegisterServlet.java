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
        response.sendRedirect("Register.jsp");
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

        UsersDao dao = new UsersDao();

        if (dao.checkUserExists(username)) {
            request.setAttribute("error", "Tên đăng nhập đã tồn tại!");
            request.getRequestDispatcher("Register.jsp").forward(request, response);
        } else {
            // Tùy ý thêm mã hóa mật khẩu tại đây nếu muốn bảo mật hơn
            Users newUser = new Users(username, password, fullname, email, "user");

            boolean success = dao.register(newUser);
            if (success) {
                response.sendRedirect(request.getContextPath() + "/user/Login.jsp");
            } else {
                request.setAttribute("error", "Đăng ký thất bại. Vui lòng thử lại.");
                request.getRequestDispatcher("Register.jsp").forward(request, response);
            }
        }
    }

    @Override
    public String getServletInfo() {
        return "Xử lý đăng ký người dùng mới.";
    }
}
