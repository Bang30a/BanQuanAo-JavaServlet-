package control.admin;

import dao.SizeDao;
import entity.Size;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/admin/SizesManagerServlet")
public class SizesManagerServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        SizeDao dao = new SizeDao();
        Size size = null;

        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            action = "List";
        }

        try {
            switch (action) {
                case "SaveOrUpdate":
                    String idStr = request.getParameter("id");
                    String label = request.getParameter("sizeLabel");

                    int id = (idStr != null && !idStr.isEmpty()) ? Integer.parseInt(idStr) : 0;
                    size = new Size(id, label);

                    if (id == 0 || dao.getSizeById(id) == null) {
                        dao.insertSize(size);
                    } else {
                        dao.updateSize(size);
                    }

                    request.setAttribute("list", dao.getAllSizes());
                    request.setAttribute("count", dao.getAllSizes().size());
                    request.getRequestDispatcher("/admin/View-sizes.jsp").forward(request, response);
                    break;

                case "AddOrEdit":
                    String idParam = request.getParameter("id");
                    int sizeId = (idParam != null && !idParam.isEmpty()) ? Integer.parseInt(idParam) : 0;
                    size = dao.getSizeById(sizeId);
                    if (size == null) size = new Size();

                    request.setAttribute("SIZE", size);
                    request.setAttribute("ACTION", "SaveOrUpdate");
                    request.getRequestDispatcher("/admin/SizesManager.jsp").forward(request, response);
                    break;

                case "Delete":
                    idParam = request.getParameter("id");
                    if (idParam != null && !idParam.isEmpty()) {
                        sizeId = Integer.parseInt(idParam);
                        dao.deleteSize(sizeId);
                    }
                    request.setAttribute("list", dao.getAllSizes());
                    request.setAttribute("count", dao.getAllSizes().size());
                    request.getRequestDispatcher("/admin/View-sizes.jsp").forward(request, response);
                    break;

                case "List":
                default:
                    request.setAttribute("list", dao.getAllSizes());
                    request.setAttribute("count", dao.getAllSizes().size());
                    request.getRequestDispatcher("/admin/View-sizes.jsp").forward(request, response);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
}
