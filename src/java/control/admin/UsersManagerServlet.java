package control.admin;

import dao.UsersDao;
import entity.Users;
import service.UserService;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

// 🔴 1. SỬA DÒNG NÀY: Thêm /admin/ vào trước
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
            request.setAttribute("error", "Lỗi: " + e.getMessage());
            handleList(request, response);
        }
    }

    private void handleSaveOrUpdate(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        // ... (Code lấy param giữ nguyên) ...
        String idStr = request.getParameter("id");
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String role = request.getParameter("role");
        String password = request.getParameter("password");
        String fullname = request.getParameter("fullname");

        int id = 0;
        try { if (idStr != null && !idStr.isEmpty()) id = Integer.parseInt(idStr); } catch (NumberFormatException e) {}
        
        Users user = new Users(id, username, password, fullname, email, role);
        userService.saveOrUpdateUser(user);

        // 🔴 2. SỬA REDIRECT: Thêm getContextPath() + /admin/
        response.sendRedirect(request.getContextPath() + "/admin/UsersManagerServlet?action=List");
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String idParam = request.getParameter("id");
        try {
            if (idParam != null && !idParam.isEmpty()) {
                userService.deleteUser(Integer.parseInt(idParam));
            }
        } catch (Exception e) { e.printStackTrace(); }

        // 🔴 3. SỬA REDIRECT
        response.sendRedirect(request.getContextPath() + "/admin/UsersManagerServlet?action=List");
    }

    private void handleAddOrEdit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        int userId = 0;
        try { if (idParam != null && !idParam.isEmpty()) userId = Integer.parseInt(idParam); } catch (NumberFormatException e) {}

        Users user = userService.getUserForEdit(userId); 
        request.setAttribute("USER", user);
        request.setAttribute("ACTION", "SaveOrUpdate");
        
        // Forward giữ nguyên vì file JSP nằm đúng chỗ rồi
        request.getRequestDispatcher("/admin/users/UsersManager.jsp").forward(request, response);
    }

    private void handleList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Users> userList = userService.getAllUsers(); 
        
        // 🔴 QUAN TRỌNG: Kiểm tra bên file JSP (View-users.jsp) vòng lặp đang dùng biến tên gì?
        // Nếu bên đó là items="${USERS}" thì dòng dưới ĐÚNG.
        request.setAttribute("USERS", userList); 
        
        request.getRequestDispatcher("/admin/users/View-users.jsp").forward(request, response);
    }

    // ... (doGet, doPost giữ nguyên) ...
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { processRequest(request, response); }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { processRequest(request, response); }
}