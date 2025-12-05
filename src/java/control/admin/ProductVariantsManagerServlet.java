package control.admin;

import dao.ProductVariantDao;
import entity.ProductVariants;
import service.ProductVariantService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/admin/ProductVariantsManagerServlet")
public class ProductVariantsManagerServlet extends HttpServlet {

    private ProductVariantService variantService;
    
    // Biến DAO để hỗ trợ test
    private ProductVariantDao variantDao;

    public void setVariantDao(ProductVariantDao variantDao) {
        this.variantDao = variantDao;
    }

    private ProductVariantDao getVariantDao() {
        if (variantDao == null) {
            variantDao = new ProductVariantDao();
        }
        return variantDao;
    }

    @Override
    public void init() throws ServletException {
        ProductVariantDao dao = new ProductVariantDao();
        this.variantService = new ProductVariantService(dao);
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
                case "SaveOrUpdate": handleSaveOrUpdate(request, response); break;
                case "Create":    
                case "Edit":        
                case "AddOrEdit": handleAddOrEdit(request, response); break;
                case "Delete": handleDelete(request, response); break;
                case "List": default: handleList(request, response); break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Có lỗi hệ thống xảy ra: " + e.getMessage());
            handleList(request, response); 
        }
    }

    private void handleSaveOrUpdate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        int id = parseInt(request.getParameter("id"));
        int productId = parseInt(request.getParameter("productId"));
        int sizeId = parseInt(request.getParameter("sizeId"));
        int stock = parseInt(request.getParameter("stock"));
        
        double price = parseDouble(request.getParameter("price"));

        // Kiểm tra tồn tại (Logic cộng dồn)
        if (id == 0) {
            ProductVariantDao dao = getVariantDao(); 
            ProductVariants existingVariant = dao.checkExist(productId, sizeId);
            
            if (existingVariant != null) {
                id = existingVariant.getId();
                stock += existingVariant.getStock();
            }
        }

        ProductVariants variant = new ProductVariants(id, productId, sizeId, stock, price, "", "");
        
        // --- [ĐOẠN SỬA QUAN TRỌNG] ---
        // Gọi service và lấy kết quả true/false
        boolean isSuccess = variantService.saveOrUpdateVariant(variant);

        if (isSuccess) {
            // Nếu thành công: Quay về danh sách
            response.sendRedirect(request.getContextPath() + "/admin/ProductVariantsManagerServlet?action=List");
        } else {
            // Nếu thất bại: Báo lỗi và giữ nguyên trang nhập liệu
            request.setAttribute("error", "Thêm thất bại! Vui lòng kiểm tra: ID Sản phẩm hoặc ID Size có tồn tại trong Database không?");
            request.setAttribute("VARIANT", variant); // Giữ lại dữ liệu vừa nhập
            request.setAttribute("ACTION", "SaveOrUpdate");
            
            // Lưu ý: Đường dẫn này phải CHÍNH XÁC nơi bạn lưu file JSP form
            request.getRequestDispatcher("/admin/products/ProductVariantsManager.jsp").forward(request, response);
        }
    }

    private void handleAddOrEdit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = parseInt(request.getParameter("id"));
        ProductVariants variant = variantService.getVariantForEdit(id); 
        request.setAttribute("VARIANT", variant);
        request.setAttribute("ACTION", "SaveOrUpdate");
        request.getRequestDispatcher("/admin/products/ProductVariantsManager.jsp").forward(request, response);
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int id = parseInt(request.getParameter("id"));
        variantService.deleteVariant(id);
        response.sendRedirect(request.getContextPath() + "/admin/ProductVariantsManagerServlet?action=List");
    }

    private void handleList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        final int PAGE_SIZE = 8;
        int page = 1;
        String pageStr = request.getParameter("page");
        if (pageStr != null && !pageStr.isEmpty()) {
            try { page = Integer.parseInt(pageStr); } catch (NumberFormatException e) { page = 1; }
        }

        List<ProductVariants> fullList = variantService.getAllVariants(); 

        int totalItems = fullList.size();
        int totalPages = (int) Math.ceil((double) totalItems / PAGE_SIZE);

        if (page < 1) page = 1;
        if (page > totalPages && totalPages > 0) page = totalPages;

        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, totalItems);

        List<ProductVariants> listForPage;
        if (start > totalItems || totalItems == 0) {
            listForPage = new ArrayList<>();
        } else {
            listForPage = fullList.subList(start, end);
        }
        
        request.setAttribute("list", listForPage);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);

        request.getRequestDispatcher("/admin/products/View-product-variants.jsp").forward(request, response);
    }

    private int parseInt(String value) {
        try {
            return (value != null && !value.isEmpty()) ? Integer.parseInt(value) : 0;
        } catch (NumberFormatException e) { return 0; }
    }

    private double parseDouble(String value) {
        try {
            if (value == null || value.trim().isEmpty()) return 0.0;
            return Double.parseDouble(value);
        } catch (Exception e) { return 0.0; }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { processRequest(request, response); }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { processRequest(request, response); }
}