package control.admin;

import dao.SizeDao;
import entity.Size;
import service.SizeService; // <-- Import service

import java.io.IOException;
import java.util.List; // <-- Import
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/admin/SizesManagerServlet")
public class SizesManagerServlet extends HttpServlet {

    private SizeService sizeService; // <-- Tham chiếu đến service

    @Override
    public void init() throws ServletException {
        // Khởi tạo DAO và "tiêm" vào Service
        SizeDao dao = new SizeDao();
        this.sizeService = new SizeService(dao);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8"); // Đặt UTF-8 ở đây

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
            request.setAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    // --- Tách các action ra thành các hàm riêng cho rõ ràng ---

    private void handleSaveOrUpdate(HttpServletRequest request, HttpServletResponse response)
            throws IOException, NumberFormatException {
        
        String idStr = request.getParameter("id");
        String label = request.getParameter("sizeLabel");
        int id = (idStr != null && !idStr.isEmpty()) ? Integer.parseInt(idStr) : 0;
        
        Size size = new Size(id, label);
        
        // Gọi Service
        sizeService.saveOrUpdateSize(size);

        // **SỬA LỖI:** Dùng Redirect (PRG Pattern)
        response.sendRedirect("SizesManagerServlet?action=List");
    }

    private void handleAddOrEdit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, NumberFormatException {
        
        String idParam = request.getParameter("id");
        int sizeId = (idParam != null && !idParam.isEmpty()) ? Integer.parseInt(idParam) : 0;

        // Service tự xử lý logic (nếu sizeId=0 trả về size rỗng)
        Size size = sizeService.getSizeForEdit(sizeId); 

        request.setAttribute("SIZE", size);
        request.setAttribute("ACTION", "SaveOrUpdate");
        request.getRequestDispatcher("/admin/SizesManager.jsp").forward(request, response);
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException, NumberFormatException {
        
        String idParam = request.getParameter("id");
        if (idParam != null && !idParam.isEmpty()) {
            int sizeId = Integer.parseInt(idParam);
            sizeService.deleteSize(sizeId); // Gọi Service
        }

        // **SỬA LỖI:** Dùng Redirect (PRG Pattern)
        response.sendRedirect("SizesManagerServlet?action=List");
    }

    private void handleList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Gọi service 1 LẦN
        List<Size> sizeList = sizeService.getAllSizes(); 
        
        request.setAttribute("list", sizeList);
        request.setAttribute("count", sizeList.size()); // Lấy size từ list đã có
        request.getRequestDispatcher("/admin/View-sizes.jsp").forward(request, response);
    }

    // --- Các hàm doGet, doPost giữ nguyên ---

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
}