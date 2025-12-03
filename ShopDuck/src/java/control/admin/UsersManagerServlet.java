package control.admin;

import dao.UsersDao;
import entity.Users;
import service.UserService;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/UsersManagerServlet")
public class UsersManagerServlet extends HttpServlet {

    private UserService userService;

    @Override
    public void init() throws ServletException {
        // Khởi tạo DAO và Service
        UsersDao usersDao = new UsersDao();
        this.userService = new UserService(usersDao);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            action = "List"; // Mặc định là xem danh sách
        }

        try {
            switch (action) {
                // --- NHÓM HÀNH ĐỘNG THAY ĐỔI DỮ LIỆU (CUD) ---
                case "SaveOrUpdate":
                    handleSaveOrUpdate(request, response);
                    break;
                case "Delete":
                    handleDelete(request, response);
                    break;
                
                // --- NHÓM HÀNH ĐỘNG HIỂN THỊ FORM (R) ---
                case "AddOrEdit":
                    handleAddOrEdit(request, response);
                    break;
                
                // --- NHÓM HÀNH ĐỘNG XEM DANH SÁCH (R) ---
                case "List":
                default:
                    handleList(request, response);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi: " + e.getMessage());
            handleList(request, response);
        }
    }

    // 1. XỬ LÝ LƯU (Thêm mới hoặc Cập nhật)
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
        } catch (NumberFormatException e) {}
        
        Users user = new Users(id, username, password, fullname, email, role);
        userService.saveOrUpdateUser(user);

        // Sau khi lưu xong -> Redirect về trang danh sách (View)
        response.sendRedirect("UsersManagerServlet?action=List");
    }

    // 2. XỬ LÝ XÓA
    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        
        String idParam = request.getParameter("id");
        try {
            if (idParam != null && !idParam.isEmpty()) {
                userService.deleteUser(Integer.parseInt(idParam));
            }
        } catch (Exception e) { e.printStackTrace(); }

        // Xóa xong -> Redirect về trang danh sách (View)
        response.sendRedirect("UsersManagerServlet?action=List");
    }

    // 3. HIỂN THỊ FORM (Add/Edit) -> Trỏ về UsersManager.jsp
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
        
        // [FORWARD ĐÚNG] Chuyển đến trang FORM để nhập liệu
        request.getRequestDispatcher("/admin/users/UsersManager.jsp").forward(request, response);
    }

    // 4. HIỂN THỊ DANH SÁCH -> Trỏ về View-users.jsp
    private void handleList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        List<Users> userList = userService.getAllUsers(); 
        
        // [SỬA LỖI QUAN TRỌNG] Đặt tên attribute là "USERS" để khớp với JSP
        request.setAttribute("USERS", userList); 
        
        // [FORWARD ĐÚNG] Chuyển đến trang VIEW để xem danh sách
        request.getRequestDispatcher("/admin/users/View-users.jsp").forward(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}