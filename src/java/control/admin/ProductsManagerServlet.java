package control.admin;

import dao.ProductDao;
import entity.Products;
import service.ProductService;
import java.io.IOException;
import java.util.ArrayList; // Cần thêm import này
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/admin/ProductsManagerServlet")
public class ProductsManagerServlet extends HttpServlet {

    private ProductService productService;

    @Override
    public void init() throws ServletException {
        // Lưu ý: Đảm bảo ProductService của bạn có constructor nhận 1 tham số ProductDao
        ProductDao dao = new ProductDao();
        this.productService = new ProductService(dao); 
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) action = "List";

        try {
            switch (action) {
                case "SaveOrUpdate": handleSaveOrUpdate(request, response); break;
                case "Delete": handleDelete(request, response); break;
                case "AddOrEdit": handleAddOrEdit(request, response); break;
                case "List": default: handleList(request, response); break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi: " + e.getMessage());
            handleList(request, response);
        }
    }

    private void handleSaveOrUpdate(HttpServletRequest request, HttpServletResponse response)
            throws IOException, NumberFormatException {
        String idStr = request.getParameter("id");
        String name = request.getParameter("name");
        String desc = request.getParameter("description");
        String image = request.getParameter("image");
        double price = 0;
        try { price = Double.parseDouble(request.getParameter("price")); } catch (NumberFormatException e) {}

        int id = 0;
        try { if (idStr != null && !idStr.isEmpty()) id = Integer.parseInt(idStr); } catch (NumberFormatException e) {}
        
        Products product = new Products(id, name, desc, price, image);
        productService.saveOrUpdateProduct(product);

        response.sendRedirect(request.getContextPath() + "/admin/ProductsManagerServlet?action=List");
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException, NumberFormatException {
        String idParam = request.getParameter("id");
        try {
            if (idParam != null && !idParam.isEmpty()) {
                productService.deleteProduct(Integer.parseInt(idParam));
            }
        } catch (NumberFormatException e) { e.printStackTrace(); }

        response.sendRedirect(request.getContextPath() + "/admin/ProductsManagerServlet?action=List");
    }

    private void handleAddOrEdit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, NumberFormatException {
        String idParam = request.getParameter("id");
        int productId = 0;
        try { if (idParam != null && !idParam.isEmpty()) productId = Integer.parseInt(idParam); } catch (NumberFormatException e) {}
        
        Products product = productService.getProductForEdit(productId); 
        request.setAttribute("PRODUCTS", product);
        request.setAttribute("ACTION", "SaveOrUpdate");
        
        request.getRequestDispatcher("/admin/products/ProductsManager.jsp").forward(request, response);
    }

    // [ĐÃ SỬA] Hàm xử lý danh sách + Phân trang + Tìm kiếm
    private void handleList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 1. Cấu hình số lượng sản phẩm mỗi trang
        final int PAGE_SIZE = 8; 

        // 2. Xác định trang hiện tại (mặc định là 1)
        int page = 1;
        String pageStr = request.getParameter("page");
        if (pageStr != null && !pageStr.isEmpty()) {
            try {
                page = Integer.parseInt(pageStr);
            } catch (NumberFormatException e) {
                page = 1;
            }
        }

        // 3. Lấy TẤT CẢ sản phẩm (tìm kiếm hoặc lấy hết)
        String keyword = request.getParameter("keyword");
        List<Products> fullList;

        if (keyword != null && !keyword.trim().isEmpty()) {
            fullList = productService.searchProducts(keyword.trim());
        } else {
            fullList = productService.getAllProducts();
        }

        // 4. Tính toán phân trang
        int totalProducts = fullList.size(); // Tổng số sản phẩm
        
        // Tính tổng số trang (làm tròn lên)
        int totalPages = (int) Math.ceil((double) totalProducts / PAGE_SIZE);

        // Kiểm tra logic trang (không để page < 1 hoặc page > totalPages)
        if (page < 1) page = 1;
        if (page > totalPages && totalPages > 0) page = totalPages;

        // 5. Cắt danh sách (subList) để lấy đúng 8 sản phẩm cho trang hiện tại
        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, totalProducts);

        List<Products> listForPage;
        if (start > totalProducts || totalProducts == 0) {
            listForPage = new ArrayList<>(); // Danh sách rỗng
        } else {
            // Lấy danh sách con từ start đến end
            listForPage = fullList.subList(start, end);
        }

        // 6. Đẩy dữ liệu sang JSP
        request.setAttribute("PRODUCTS", listForPage); // Chỉ gửi list 8 sản phẩm
        request.setAttribute("currentPage", page);     // Trang hiện tại
        request.setAttribute("totalPages", totalPages); // Tổng số trang
        request.setAttribute("keyword", keyword);      // Từ khóa tìm kiếm để giữ lại trong input

        request.getRequestDispatcher("/admin/products/View-products.jsp").forward(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { processRequest(request, response); }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { processRequest(request, response); }
}