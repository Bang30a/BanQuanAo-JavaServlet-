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
                case "SaveOrUpdate":
                    handleSaveOrUpdate(request, response);
                    break;
                
                case "Create":    
                case "Edit":        
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
            handleList(request, response); 
        }
    }

    private void handleSaveOrUpdate(HttpServletRequest request, HttpServletResponse response)
            throws IOException, NumberFormatException {
        
        int id = parseInt(request.getParameter("id"));
        int productId = parseInt(request.getParameter("productId"));
        int sizeId = parseInt(request.getParameter("sizeId"));
        int stock = parseInt(request.getParameter("stock"));
        double price = Double.parseDouble(request.getParameter("price"));

        // [LOGIC MỚI] Kiểm tra tồn tại để cộng dồn nếu đang là thao tác Thêm mới (id == 0)
        if (id == 0) {
            ProductVariantDao dao = new ProductVariantDao(); // Khởi tạo DAO để check
            ProductVariants existingVariant = dao.checkExist(productId, sizeId);
            
            if (existingVariant != null) {
                // Nếu đã tồn tại:
                // 1. Gán ID của cái cũ để Service hiểu là Update
                id = existingVariant.getId();
                // 2. Cộng dồn số lượng
                stock += existingVariant.getStock();
                // Giá: Lấy giá mới user vừa nhập (hoặc giữ giá cũ tùy nghiệp vụ, ở đây ta ưu tiên giá mới)
            }
        }

        ProductVariants variant = new ProductVariants(id, productId, sizeId, stock, price, "", "");

        // Service sẽ tự động gọi Update nếu id > 0, hoặc Insert nếu id == 0
        variantService.saveOrUpdateVariant(variant);

        response.sendRedirect(request.getContextPath() + "/admin/ProductVariantsManagerServlet?action=List");
    }

    private void handleAddOrEdit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, NumberFormatException {
        
        int id = parseInt(request.getParameter("id"));
        ProductVariants variant = variantService.getVariantForEdit(id); 

        request.setAttribute("VARIANT", variant);
        request.setAttribute("ACTION", "SaveOrUpdate");

        request.getRequestDispatcher("/admin/products/ProductVariantsManager.jsp").forward(request, response);
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException, NumberFormatException {
        
        int id = parseInt(request.getParameter("id"));
        variantService.deleteVariant(id);

        response.sendRedirect(request.getContextPath() + "/admin/ProductVariantsManagerServlet?action=List");
    }

    private void handleList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 1. Cấu hình số lượng mỗi trang
        final int PAGE_SIZE = 8;

        // 2. Xác định trang hiện tại
        int page = 1;
        String pageStr = request.getParameter("page");
        if (pageStr != null && !pageStr.isEmpty()) {
            try {
                page = Integer.parseInt(pageStr);
            } catch (NumberFormatException e) {
                page = 1;
            }
        }

        // 3. Lấy toàn bộ danh sách
        List<ProductVariants> fullList = variantService.getAllVariants(); 

        // 4. Tính toán phân trang
        int totalItems = fullList.size();
        int totalPages = (int) Math.ceil((double) totalItems / PAGE_SIZE);

        if (page < 1) page = 1;
        if (page > totalPages && totalPages > 0) page = totalPages;

        // 5. Cắt danh sách (subList)
        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, totalItems);

        List<ProductVariants> listForPage;
        if (start > totalItems || totalItems == 0) {
            listForPage = new ArrayList<>();
        } else {
            listForPage = fullList.subList(start, end);
        }
        
        // 6. Đẩy dữ liệu sang JSP
        request.setAttribute("list", listForPage); // Chỉ gửi list của trang hiện tại
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);

        request.getRequestDispatcher("/admin/products/View-product-variants.jsp").forward(request, response);
    }

    private int parseInt(String value) {
        try {
            return (value != null && !value.isEmpty()) ? Integer.parseInt(value) : 0;
        } catch (NumberFormatException e) {
            return 0; 
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