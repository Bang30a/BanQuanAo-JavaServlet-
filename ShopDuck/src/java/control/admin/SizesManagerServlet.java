package control.admin;

import dao.SizeDao;
import entity.Size;
import service.SizeService; 

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/admin/SizesManagerServlet")
public class SizesManagerServlet extends HttpServlet {

    private SizeService sizeService;

    @Override
    public void init() throws ServletException {
        SizeDao dao = new SizeDao();
        this.sizeService = new SizeService(dao);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            action = "List";
        }

        try {
            switch (action) {
                case "SaveOrUpdate":
                    handleSaveOrUpdate(request, response);
                    break;
                case "AddOrEdit":
                    handleAddOrEdit(request, response);
                    break;
                case "Delete":
                    handleDelete(request, response);
                    break;
                case "List":
                default:
                    handleList(request, response);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void handleSaveOrUpdate(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String idStr = request.getParameter("id");
        String label = request.getParameter("sizeLabel");
        int id = (idStr != null && !idStr.isEmpty()) ? Integer.parseInt(idStr) : 0;
        
        Size size = new Size(id, label);
        sizeService.saveOrUpdateSize(size);
        response.sendRedirect("SizesManagerServlet?action=List");
    }

    private void handleAddOrEdit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        int sizeId = (idParam != null && !idParam.isEmpty()) ? Integer.parseInt(idParam) : 0;

        Size size = sizeService.getSizeForEdit(sizeId); 

        request.setAttribute("SIZE", size);
        request.setAttribute("ACTION", "SaveOrUpdate");
        
        // [SỬA ĐƯỜNG DẪN] Thêm /products/ vào đường dẫn
        request.getRequestDispatcher("/admin/products/SizesManager.jsp").forward(request, response);
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String idParam = request.getParameter("id");
        if (idParam != null && !idParam.isEmpty()) {
            sizeService.deleteSize(Integer.parseInt(idParam));
        }
        response.sendRedirect("SizesManagerServlet?action=List");
    }

    private void handleList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        List<Size> sizeList = sizeService.getAllSizes(); 
        request.setAttribute("list", sizeList);
        
        // [SỬA ĐƯỜNG DẪN] Thêm /products/ vào đường dẫn cho khớp với ảnh của bạn
        request.getRequestDispatcher("/admin/products/View-sizes.jsp").forward(request, response);
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