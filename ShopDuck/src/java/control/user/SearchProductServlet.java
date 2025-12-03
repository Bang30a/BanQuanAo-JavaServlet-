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
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "UserSearchProductServlet", urlPatterns = {"/user/search-products"})
public class SearchProductServlet extends HttpServlet {

    private ProductService productService;

    @Override
    public void init() throws ServletException {
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
            response.sendRedirect(request.getContextPath() + "/user/view-products");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            handleRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(SearchProductServlet.class.getName()).log(Level.SEVERE, null, ex);
            response.sendRedirect(request.getContextPath() + "/user/view-products");
        }
    }

    private void handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        if ("detail".equalsIgnoreCase(action)) {
            handleDetailAction(request, response);
        } else {
            handleSearchAction(request, response);
        }
    }

    private void handleDetailAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // ... Giữ nguyên phần chi tiết sản phẩm ...
        String idStr = request.getParameter("id");
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
        String keyword = request.getParameter("keyword");
        if (keyword == null) keyword = "";

        // [FIX LỖI PHÔNG CHỮ]
        // Nếu là phương thức GET, server thường mã hóa sai (ISO-8859-1)
        // Ta cần chuyển đổi lại sang UTF-8
        if (request.getMethod().equalsIgnoreCase("GET")) {
            keyword = new String(keyword.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        }

        List<Products> results = productService.searchProducts(keyword);
        request.setAttribute("productList", results);
        request.setAttribute("keyword", keyword);

        request.getRequestDispatcher("/user/product/searchResult.jsp").forward(request, response);
    }
}