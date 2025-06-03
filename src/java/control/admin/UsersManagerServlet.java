package control.admin;

import dao.UsersDao;
import entity.Users;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/admin/UsersManagerServlet")
public class UsersManagerServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        UsersDao dao = new UsersDao();
        Users user = null;

        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            action = "List";
        }

        try {
            switch (action) {
                case "SaveOrUpdate": {
                    String idStr = request.getParameter("id");
                    String username = request.getParameter("username");
                    String email = request.getParameter("email");
                    String role = request.getParameter("role");
                    String password = request.getParameter("password");
                    String fullname = request.getParameter("fullname"); // Thêm dòng này

                    int id = (idStr != null && !idStr.isEmpty()) ? Integer.parseInt(idStr) : 0;
                    user = new Users(id, username, email, password, role, fullname);
                    user.setFullname(fullname); // Set giá trị fullname

                    if (id == 0 || dao.getUserById(id) == null) {
                        dao.insert(user);
                    } else {
                        dao.updateUser(user);
                    }

                    request.setAttribute("list", dao.getAllUsers());
                    request.setAttribute("count", dao.getAllUsers().size());
                    request.getRequestDispatcher("/admin/UsersManager.jsp").forward(request, response);
                    break;
                }
                case "List": {
                    request.setAttribute("list", dao.getAllUsers());
                    request.setAttribute("count", dao.getAllUsers().size());
                    request.getRequestDispatcher("/admin/View-users.jsp").forward(request, response);
                    break;
                }

                case "AddOrEdit": {
                    String idParam = request.getParameter("id");
                    int userId = 0;
                    if (idParam != null && !idParam.isEmpty()) {
                        userId = Integer.parseInt(idParam);
                        user = dao.getUserById(userId);
                    }
                    if (user == null) {
                        user = new Users();
                    }

                    request.setAttribute("USER", user); // 
                    request.setAttribute("ACTION", "SaveOrUpdate");
                    request.getRequestDispatcher("/admin/UsersManager.jsp").forward(request, response);
                    break;
                }

                case "Delete": {
                    String idParam = request.getParameter("id");
                    if (idParam != null && !idParam.isEmpty()) {
                        int userId = Integer.parseInt(idParam);
                        dao.deleteUser(userId);
                    }

                    request.setAttribute("list", dao.getAllUsers());
                    request.setAttribute("count", dao.getAllUsers().size());
                    request.getRequestDispatcher("/admin/View-users.jsp").forward(request, response);
                    break;
                }

                default: {
                    request.setAttribute("error", "Hành động không hợp lệ!");
                    request.getRequestDispatcher("/error.jsp").forward(request, response);
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
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

    @Override
    public String getServletInfo() {
        return "User management servlet";
    }
}
