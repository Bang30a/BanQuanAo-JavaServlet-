package control;

import dao.UsersDao;
import entity.Users;
import service.LoginResult;
import service.LoginService;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet(name = "LoginServlet", urlPatterns = {"/LoginServlet"})
public class LoginServlet extends HttpServlet {

    private LoginService loginService;

    @Override
    public void init() throws ServletException {
        UsersDao usersDao = new UsersDao();
        this.loginService = new LoginService(usersDao);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // [SỬA PATH] Trỏ đúng vào thư mục user/auth/
        response.sendRedirect(request.getContextPath() + "/user/auth/Login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        HttpSession session = request.getSession();
        LoginResult result = loginService.login(username, password);

        // Lấy URL trang trước đó (nếu người dùng bị chặn khi đang truy cập trang nào đó)
        String redirectUrl = (String) session.getAttribute("redirectAfterLogin");

        switch (result.getStatus()) {
            case SUCCESS_ADMIN:
                session.setAttribute("user", result.getUser());
                session.removeAttribute("loginError");
                session.removeAttribute("redirectAfterLogin");
                
                // [SỬA PATH] Admin về Dashboard mới
                response.sendRedirect(request.getContextPath() + "/admin/dashboard/index.jsp");
                break;

            case SUCCESS_USER:
                session.setAttribute("user", result.getUser());
                session.removeAttribute("loginError");
                session.removeAttribute("redirectAfterLogin");

                // --- LOGIC ĐIỀU HƯỚNG ---
                if (redirectUrl != null && !redirectUrl.isEmpty()) {
                    response.sendRedirect(redirectUrl); // Quay lại trang cũ
                } else {
                    // [QUAN TRỌNG] Về Servlet trang chủ (/user/view-products)
                    response.sendRedirect(request.getContextPath() + "/user/view-products");
                }
                break;

            case FAILED_CREDENTIALS:
                session.setAttribute("loginError", "❌ Sai tên đăng nhập hoặc mật khẩu!");
                // [SỬA PATH] Về lại form login
                response.sendRedirect(request.getContextPath() + "/user/auth/Login.jsp");
                break;

            case FAILED_INVALID_ROLE:
                session.setAttribute("loginError", "Quyền truy cập không hợp lệ.");
                response.sendRedirect(request.getContextPath() + "/user/auth/Login.jsp");
                break;
        }
    }
}