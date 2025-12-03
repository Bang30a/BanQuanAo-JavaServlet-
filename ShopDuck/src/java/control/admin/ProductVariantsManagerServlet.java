package control.admin;

import dao.ProductVariantDao;
import entity.ProductVariants;
import service.ProductVariantService; // <-- Import service

import java.io.IOException;
import java.util.List; // <-- Import
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/admin/ProductVariantsManagerServlet")
public class ProductVariantsManagerServlet extends HttpServlet {

    private ProductVariantService variantService; // <-- Tham chiếu đến service

    @Override
    public void init() throws ServletException {
        // Khởi tạo DAO và "tiêm" vào Service
        ProductVariantDao dao = new ProductVariantDao();
        this.variantService = new ProductVariantService(dao);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8"); // Đặt UTF-8

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
        
        int id = parseInt(request.getParameter("id"));
        int productId = parseInt(request.getParameter("productId"));
        int sizeId = parseInt(request.getParameter("sizeId"));
        int stock = parseInt(request.getParameter("stock"));
        double price = Double.parseDouble(request.getParameter("price"));

        ProductVariants variant = new ProductVariants(id, productId, sizeId, stock, price, "", "");

        // Gọi Service
        variantService.saveOrUpdateVariant(variant);

        // **SỬA LỖI:** Dùng Redirect (PRG Pattern)
        response.sendRedirect("ProductVariantsManagerServlet?action=List");
    }

    private void handleAddOrEdit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, NumberFormatException {
        
        int id = parseInt(request.getParameter("id"));
        
        // Service tự xử lý logic (nếu id=0 trả về variant rỗng)
        ProductVariants variant = variantService.getVariantForEdit(id); 

        request.setAttribute("VARIANT", variant);
        request.setAttribute("ACTION", "SaveOrUpdate");
        request.getRequestDispatcher("/admin/ProductVariantsManager.jsp").forward(request, response);
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException, NumberFormatException {
        
        int id = parseInt(request.getParameter("id"));
        
        // Gọi Service
        variantService.deleteVariant(id);

        // **SỬA LỖI:** Dùng Redirect (PRG Pattern)
        // Chuyển hướng về action=List, nó sẽ tự động lấy list mới và forward đến trang view
        response.sendRedirect("ProductVariantsManagerServlet?action=List");
    }

    private void handleList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Gọi service
        List<ProductVariants> list = variantService.getAllVariants(); 
        
        request.setAttribute("list", list);
        request.getRequestDispatcher("/admin/View-product-variants.jsp").forward(request, response);
    }

    /**
     * Hàm tiện ích để parse an toàn, trả về 0 nếu null hoặc rỗng.
     */
    private int parseInt(String value) {
        return (value != null && !value.isEmpty()) ? Integer.parseInt(value) : 0;
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