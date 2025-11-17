package control;

import dao.UsersDao;
import entity.Users;
import service.LoginResult; 
import service.LoginService; 

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/login")
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
        // Chuyển hướng đến /user/Login.jsp thay vì /Login.jsp
        response.sendRedirect(request.getContextPath() + "/user/Login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        HttpSession session = request.getSession(); // <-- Lấy session

        LoginResult result = loginService.login(username, password);

        // Lấy URL cần quay lại (nếu có)
        String redirectUrl = (String) session.getAttribute("redirectAfterLogin");

        switch (result.getStatus()) {
            
            case SUCCESS_ADMIN:
                session.setAttribute("user", result.getUser());
                
                // Xóa lỗi (nếu có) và URL quay lại
                session.removeAttribute("loginError");
                session.removeAttribute("redirectAfterLogin");
                
                // Nếu admin cố thêm vào giỏ hàng, cũng chỉ về dashboard
                response.sendRedirect(request.getContextPath() + "/admin/Dashboard.jsp");
                break;
                
            case SUCCESS_USER:
                session.setAttribute("user", result.getUser());
                
                // Xóa lỗi (nếu có) và URL quay lại
                session.removeAttribute("loginError");
                session.removeAttribute("redirectAfterLogin");

                // --- LOGIC MỚI: QUAY LẠI TRANG SẢN PHẨM ---
                if (redirectUrl != null && !redirectUrl.isEmpty()) {
                    response.sendRedirect(redirectUrl); // Quay lại trang sản phẩm
                } else {
                    // Mặc định: Về trang chủ
                    response.sendRedirect(request.getContextPath() + "/user/view-products");
                }
                break;

            case FAILED_CREDENTIALS:
                // Sửa lỗi: Gửi lỗi qua session (vì Login.jsp đang ở /user/)
                // thay vì request.setAttribute
                session.setAttribute("loginError", "❌ Sai tên đăng nhập hoặc mật khẩu!");
                response.sendRedirect(request.getContextPath() + "/user/Login.jsp");
                break;
                
            case FAILED_INVALID_ROLE:
                session.setAttribute("loginError", "Quyền truy cập không hợp lệ.");
                response.sendRedirect(request.getContextPath() + "/user/Login.jsp");
                break;
        }
    }
}