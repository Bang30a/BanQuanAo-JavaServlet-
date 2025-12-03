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
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        String label = request.getParameter("sizeLabel");
        int id = 0;
        try {
            if (idStr != null && !idStr.isEmpty()) {
                id = Integer.parseInt(idStr);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        
        Size size = new Size(id, label);
        
        // --- SỬA LOGIC Ở ĐÂY ĐỂ PASS UNIT TEST ---
        // Gọi Service và hứng kết quả (String) thay vì void
        String result = sizeService.saveOrUpdateSize(size);

        if ("SUCCESS".equals(result)) {
            // Thành công -> Redirect về danh sách
            response.sendRedirect(request.getContextPath() + "/admin/SizesManagerServlet?action=List");
        } else {
            // Thất bại -> Forward lại form để báo lỗi
            request.setAttribute("ERROR", result); // Đây là cái Test Case đang tìm kiếm
            request.setAttribute("SIZE", size);    // Giữ lại dữ liệu cũ
            request.setAttribute("ACTION", "SaveOrUpdate");
            
            // Forward về đường dẫn /admin/products/ như bạn yêu cầu
            request.getRequestDispatcher("/admin/products/SizesManager.jsp").forward(request, response);
        }
    }

    private void handleAddOrEdit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        int sizeId = 0;
        try {
            if (idParam != null && !idParam.isEmpty()) {
                sizeId = Integer.parseInt(idParam);
            }
        } catch (NumberFormatException e) {}

        Size size = sizeService.getSizeForEdit(sizeId); 

        request.setAttribute("SIZE", size);
        request.setAttribute("ACTION", "SaveOrUpdate");
        
        // Giữ nguyên đường dẫn của bạn
        request.getRequestDispatcher("/admin/products/SizesManager.jsp").forward(request, response);
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String idParam = request.getParameter("id");
        try {
            if (idParam != null && !idParam.isEmpty()) {
                sizeService.deleteSize(Integer.parseInt(idParam));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        response.sendRedirect(request.getContextPath() + "/admin/SizesManagerServlet?action=List");
    }

    private void handleList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        List<Size> sizeList = sizeService.getAllSizes(); 
        request.setAttribute("list", sizeList);
        
        // Giữ nguyên đường dẫn của bạn
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