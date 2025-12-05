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
    
    // Thêm biến DAO để có thể kiểm tra user tồn tại
    private UsersDao usersDao;

    // Getter cho DAO (Hỗ trợ Mock test sau này nếu cần)
    private UsersDao getUsersDao() {
        if (usersDao == null) {
            usersDao = new UsersDao();
        }
        return usersDao;
    }

    @Override
    public void init() throws ServletException {
        // Khởi tạo service
        this.loginService = new LoginService(getUsersDao());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/user/auth/Login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        HttpSession session = request.getSession();
        
        // --- [THÊM MỚI] CHECK TÀI KHOẢN TỒN TẠI TRƯỚC ---
        UsersDao dao = getUsersDao();
        if (!dao.checkUserExists(username)) {
            // Nếu không tìm thấy user trong DB -> Báo lỗi cụ thể
            session.setAttribute("loginError", "Tài khoản không tồn tại!");
            response.sendRedirect(request.getContextPath() + "/user/auth/Login.jsp");
            return; // Dừng luôn, không cần check pass
        }
        // ------------------------------------------------

        // Nếu tài khoản có tồn tại, mới check tiếp mật khẩu qua Service
        LoginResult result = loginService.login(username, password);

        String redirectUrl = (String) session.getAttribute("redirectAfterLogin");

        switch (result.getStatus()) {
            case SUCCESS_ADMIN:
                session.setAttribute("user", result.getUser());
                session.removeAttribute("loginError");
                session.removeAttribute("redirectAfterLogin");
                response.sendRedirect(request.getContextPath() + "/admin/dashboard/index.jsp");
                break;

            case SUCCESS_USER:
                session.setAttribute("user", result.getUser());
                session.removeAttribute("loginError");
                session.removeAttribute("redirectAfterLogin");

                if (redirectUrl != null && !redirectUrl.isEmpty()) {
                    response.sendRedirect(redirectUrl);
                } else {
                    response.sendRedirect(request.getContextPath() + "/user/view-products");
                }
                break;

            case FAILED_CREDENTIALS:
                // Lúc này chắc chắn là Sai Mật Khẩu (vì đã check tồn tại ở trên rồi)
                session.setAttribute("loginError", "Mật khẩu không chính xác!");
                response.sendRedirect(request.getContextPath() + "/user/auth/Login.jsp");
                break;

            case FAILED_INVALID_ROLE:
                session.setAttribute("loginError", "Quyền truy cập không hợp lệ.");
                response.sendRedirect(request.getContextPath() + "/user/auth/Login.jsp");
                break;
        }
    }
}