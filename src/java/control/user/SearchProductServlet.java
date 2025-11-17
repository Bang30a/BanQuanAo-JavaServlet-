package control.user;

import dao.ProductDao;
import dao.ProductVariantDao; // <-- Import
import dao.SizeDao; // <-- Import
import entity.Products;
import service.ProductService;
import entity.ProductVariants; // <-- Import
import java.util.List;
import java.util.Map; // <-- Import
import java.util.HashMap; // <-- Import

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet; // <-- Import
import javax.servlet.http.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

// Dùng @WebServlet thay vì web.xml
@WebServlet(name = "UserSearchProductServlet", urlPatterns = {"/user/search-products"})
public class SearchProductServlet extends HttpServlet {

    private ProductService productService;

    @Override
    public void init() throws ServletException {
        // SỬA LỖI 1: Khởi tạo service với 3 DAO
        ProductDao productDao = new ProductDao();
        ProductVariantDao variantDao = new ProductVariantDao();
        SizeDao sizeDao = new SizeDao();
        this.productService = new ProductService(productDao, variantDao, sizeDao);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            handleRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(SearchProductServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            handleRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(SearchProductServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        if ("detail".equalsIgnoreCase(action)) {
            handleDetailAction(request, response);
        } else {
            handleSearchAction(request, response);
        }
    }

    private void handleDetailAction(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String idStr = request.getParameter("id");
        try {
            int id = Integer.parseInt(idStr);
            Products product = productService.getProductDetails(id); 
            if (product != null) {
                // SỬA LỖI 3: Lấy đủ data cho trang chi tiết
                Map<Integer, String> sizeMap = productService.getSizeMap();
                List<ProductVariants> variants = productService.getVariantsByProductId(id);
                
                request.setAttribute("product", product);
                request.setAttribute("variants", variants); // <-- Thêm
                request.setAttribute("sizeMap", sizeMap);   // <-- Thêm
                
                request.getRequestDispatcher("/user/info-products.jsp").forward(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/user/view-products");
            }
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/user/view-products");
        }
    }

    private void handleSearchAction(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

    String keyword = request.getParameter("keyword");

    // FIX: keyword null hoặc rỗng → gán chuỗi rỗng thay vì để null
    if (keyword == null) keyword = "";

    List<Products> results = productService.searchProducts(keyword);

    request.setAttribute("productList", results);
    request.setAttribute("keyword", keyword);

    request.getRequestDispatcher("/user/searchResult.jsp").forward(request, response);
}

}