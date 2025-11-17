package control.admin;

import dao.UsersDao;
import entity.Users;
import service.UserService; // <-- Import service

import java.io.IOException;
import java.util.List; // <-- Import
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/admin/UsersManagerServlet")
public class UsersManagerServlet extends HttpServlet {

    private UserService userService; // <-- Tham chiếu đến service

    @Override
    public void init() throws ServletException {
        // Khởi tạo DAO và Service MỘT LẦN khi servlet bắt đầu
        UsersDao usersDao = new UsersDao();
        this.userService = new UserService(usersDao);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        // KHÔNG tạo DAO ở đây nữa

        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            action = "List"; // Mặc định là "List"
        }

        try {
            switch (action) {
                case "SaveOrUpdate":
                    handleSaveOrUpdate(request, response);
                    break;
                case "List":
                    handleList(request, response);
                    break;
                case "AddOrEdit":
                    handleAddOrEdit(request, response);
                    break;
                case "Delete":
                    handleDelete(request, response);
                    break;
                default:
                    request.setAttribute("error", "Hành động không hợp lệ!");
                    request.getRequestDispatcher("/error.jsp").forward(request, response);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    // --- Tách các action ra thành các hàm riêng cho rõ ràng ---

    private void handleSaveOrUpdate(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        
        String idStr = request.getParameter("id");
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String role = request.getParameter("role");
        String password = request.getParameter("password");
        String fullname = request.getParameter("fullname");

        int id = (idStr != null && !idStr.isEmpty()) ? Integer.parseInt(idStr) : 0;
        
        // Tạo đối tượng User
        Users user = new Users(id, username, email, password, role, fullname);
        user.setFullname(fullname); // Đảm bảo fullname được set (nếu constructor không có)

        // Gọi Service
        userService.saveOrUpdateUser(user);

        // **QUAN TRỌNG: Dùng Redirect (PRG Pattern) để tránh lặp lại hành động khi F5**
        response.sendRedirect("UsersManagerServlet?action=List");
    }

    private void handleList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Gọi service 1 LẦN
        List<Users> userList = userService.getAllUsers(); 
        
        request.setAttribute("list", userList);
        request.setAttribute("count", userList.size()); // Lấy size từ list đã có
        request.getRequestDispatcher("/admin/View-users.jsp").forward(request, response);
    }

    private void handleAddOrEdit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String idParam = request.getParameter("id");
        int userId = 0;
        if (idParam != null && !idParam.isEmpty()) {
            userId = Integer.parseInt(idParam);
        }

        // Service sẽ tự xử lý logic (nếu userId=0 trả về user rỗng)
        Users user = userService.getUserForEdit(userId); 

        request.setAttribute("USER", user);
        request.setAttribute("ACTION", "SaveOrUpdate");
        request.getRequestDispatcher("/admin/UsersManager.jsp").forward(request, response);
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        
        String idParam = request.getParameter("id");
        if (idParam != null && !idParam.isEmpty()) {
            int userId = Integer.parseInt(idParam);
            userService.deleteUser(userId); // Gọi Service
        }

        // **QUAN TRỌNG: Dùng Redirect (PRG Pattern)**
        response.sendRedirect("UsersManagerServlet?action=List");
    }

    // --- Các hàm doGet, doPost, getServletInfo giữ nguyên ---

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

    @Override
    public String getServletInfo() {
        return "User management servlet";
    }
}