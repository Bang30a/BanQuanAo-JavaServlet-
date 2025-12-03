package control.admin;

import dao.UsersDao;
import entity.Users;
import service.UserService;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

// 1. SỬA URL PATTERN theo yêu cầu
@WebServlet("/admin/UsersManagerServlet")
public class UsersManagerServlet extends HttpServlet {

    private UserService userService;

    // 2. SỬA INIT: Khởi tạo Service tại đây
    @Override
    public void init() throws ServletException {
        UsersDao usersDao = new UsersDao();
        this.userService = new UserService(usersDao);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) action = "List";

        try {
            switch (action) {
                case "SaveOrUpdate": handleSaveOrUpdate(request, response); break;
                case "Delete": handleDelete(request, response); break;
                case "AddOrEdit": handleAddOrEdit(request, response); break;
                case "List": default: handleList(request, response); break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi hệ thống: " + e.getMessage());
            handleList(request, response);
        }
    }

    private void handleSaveOrUpdate(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        // Lấy dữ liệu từ form
        String idStr = request.getParameter("id");
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String role = request.getParameter("role");
        String password = request.getParameter("password");
        String fullname = request.getParameter("fullname");

        int id = 0;
        try { 
            if (idStr != null && !idStr.isEmpty()) id = Integer.parseInt(idStr); 
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        
        Users user = new Users();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);
        user.setFullname(fullname);
        user.setEmail(email);
        user.setRole(role);

        // --- QUAN TRỌNG: Logic Validation để vượt qua Unit Test ---
        // Gọi Service và nhận về thông báo kết quả (String)
        String result = userService.saveOrUpdateUser(user);

        if ("SUCCESS".equals(result)) {
            // Thành công -> Redirect đúng đường dẫn bạn yêu cầu
            response.sendRedirect(request.getContextPath() + "/admin/UsersManagerServlet?action=List");
        } else {
            // Thất bại -> Forward về lại form nhập và hiện lỗi (Test Case yêu cầu điều này)
            request.setAttribute("ERROR", result); 
            request.setAttribute("USER", user);
            request.setAttribute("ACTION", "SaveOrUpdate");
            // Đường dẫn JSP theo cấu trúc mới của bạn
            request.getRequestDispatcher("/admin/users/UsersManager.jsp").forward(request, response);
        }
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String idParam = request.getParameter("id");
        try {
            if (idParam != null && !idParam.isEmpty()) {
                userService.deleteUser(Integer.parseInt(idParam));
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }

        // Redirect đúng đường dẫn
        response.sendRedirect(request.getContextPath() + "/admin/UsersManagerServlet?action=List");
    }

    private void handleAddOrEdit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        int userId = 0;
        try { 
            if (idParam != null && !idParam.isEmpty()) userId = Integer.parseInt(idParam); 
        } catch (NumberFormatException e) {}

        Users user = userService.getUserForEdit(userId); 
        request.setAttribute("USER", user);
        request.setAttribute("ACTION", "SaveOrUpdate");
        
        // Forward đúng đường dẫn JSP mới
        request.getRequestDispatcher("/admin/users/UsersManager.jsp").forward(request, response);
    }

    private void handleList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Users> userList = userService.getAllUsers(); 
        request.setAttribute("USERS", userList); 
        
        // Forward đúng đường dẫn JSP mới
        request.getRequestDispatcher("/admin/users/View-users.jsp").forward(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { processRequest(request, response); }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { processRequest(request, response); }
}