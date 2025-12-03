package control.user;

import dao.ProductDao;
import dao.ProductVariantDao;
import dao.SizeDao;
import entity.Products;
import entity.ProductVariants;
import service.ProductService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "UserSearchProductServlet", urlPatterns = {"/user/search-products"})
public class SearchProductServlet extends HttpServlet {

    private ProductService productService;

    @Override
    public void init() throws ServletException {
        // Khởi tạo mặc định cho lúc chạy thật trên Server
        ProductDao productDao = new ProductDao();
        ProductVariantDao variantDao = new ProductVariantDao();
        SizeDao sizeDao = new SizeDao();
        this.productService = new ProductService(productDao, variantDao, sizeDao);
    }

    // [THÊM MỚI] Hàm Setter này cực quan trọng để Unit Test chèn Mock Service vào
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    @Override
    // [SỬA] Đổi protected -> public để Unit Test gọi được
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            handleRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(SearchProductServlet.class.getName()).log(Level.SEVERE, null, ex);
            response.sendRedirect(request.getContextPath() + "/user/view-products");
        }
    }

    @Override
    // [SỬA] Đổi protected -> public để Unit Test gọi được (nếu cần test doPost)
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            handleRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(SearchProductServlet.class.getName()).log(Level.SEVERE, null, ex);
            response.sendRedirect(request.getContextPath() + "/user/view-products");
        }
    }

    private void handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // [QUAN TRỌNG] Đặt mã hóa UTF-8 ngay đầu hàm
        request.setCharacterEncoding("UTF-8");
        
        String action = request.getParameter("action");

        if ("detail".equalsIgnoreCase(action)) {
            handleDetailAction(request, response);
        } else {
            handleSearchAction(request, response);
        }
    }

    private void handleDetailAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String idStr = request.getParameter("id");
        // Redirect URL mặc định nếu có lỗi
        String redirectUrl = request.getContextPath() + "/user/product/searchResult.jsp";

        try {
            int id = Integer.parseInt(idStr);
            Products product = productService.getProductDetails(id);
            
            if (product != null) {
                Map<Integer, String> sizeMap = productService.getSizeMap();
                List<ProductVariants> variants = productService.getVariantsByProductId(id);
                
                request.setAttribute("product", product);
                request.setAttribute("variants", variants);
                request.setAttribute("sizeMap", sizeMap);
                
                request.getRequestDispatcher("/user/product/info-products.jsp").forward(request, response);
            } else {
                response.sendRedirect(redirectUrl);
            }
        } catch (Exception e) {
            response.sendRedirect(redirectUrl);
        }
    }

    private void handleSearchAction(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // 1. Lấy từ khóa
        String keyword = request.getParameter("keyword");
        
        if (keyword == null) {
            keyword = "";
        }
        keyword = keyword.trim();

        // 2. Gọi Service tìm kiếm
        List<Products> results = productService.searchProducts(keyword);
        
        // 3. Gửi kết quả về JSP
        request.setAttribute("productList", results);
        request.setAttribute("keyword", keyword);

        request.getRequestDispatcher("/user/product/searchResult.jsp").forward(request, response);
    }
}