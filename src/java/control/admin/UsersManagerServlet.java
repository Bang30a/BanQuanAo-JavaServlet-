package control.admin;

import dao.UsersDao;
import entity.Users;
import service.UserService;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/admin/UsersManagerServlet")
public class UsersManagerServlet extends HttpServlet {

    private UserService userService;

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
            // [QUAN TRỌNG] Đây là nơi Test Case 9 kiểm tra
            request.setAttribute("error", "Lỗi hệ thống: " + e.getMessage());
            handleList(request, response);
        }
    }

    private void handleSaveOrUpdate(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
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

        String result = userService.saveOrUpdateUser(user);

        if ("SUCCESS".equals(result)) {
            response.sendRedirect(request.getContextPath() + "/admin/UsersManagerServlet?action=List");
        } else {
            request.setAttribute("ERROR", result); 
            request.setAttribute("USER", user);
            request.setAttribute("ACTION", "SaveOrUpdate");
            request.getRequestDispatcher("/admin/users/UsersManager.jsp").forward(request, response);
        }
    }

    // [SỬA ĐOẠN NÀY] Tách xử lý lỗi để thỏa mãn cả Test Case 8 và 9
    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String idParam = request.getParameter("id");
        
        int id = 0;
        try {
            // 1. Cố gắng parse ID
            if (idParam != null && !idParam.isEmpty()) {
                id = Integer.parseInt(idParam);
            }
        } catch (NumberFormatException e) {
            // Test Case 8: Nếu ID rác -> Catch tại đây và Redirect (không để crash)
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/UsersManagerServlet?action=List");
            return; 
        }

        // 2. Gọi Service (KHÔNG dùng try-catch ở đây)
        // Test Case 9: Nếu Service ném lỗi DB -> Nó sẽ bay ra ngoài method này
        // và được catch bởi processRequest -> Forward + Báo lỗi
        if (id > 0) {
            userService.deleteUser(id);
        }

        // 3. Nếu thành công -> Redirect
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
        
        request.getRequestDispatcher("/admin/users/UsersManager.jsp").forward(request, response);
    }

    private void handleList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Users> userList = userService.getAllUsers(); 
        request.setAttribute("USERS", userList); 
        
        request.getRequestDispatcher("/admin/users/View-users.jsp").forward(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { processRequest(request, response); }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { processRequest(request, response); }
}