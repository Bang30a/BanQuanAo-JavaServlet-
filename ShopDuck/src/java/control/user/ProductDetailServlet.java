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

@WebServlet("/user/product-detail")
public class ProductDetailServlet extends HttpServlet {
    
    private ProductService productService;

    @Override
    public void init() throws ServletException {
        // [QUAN TRỌNG 1] Phải khởi tạo đủ 3 DAO thì Service mới lấy được Size
        ProductDao productDao = new ProductDao();
        ProductVariantDao variantDao = new ProductVariantDao();
        SizeDao sizeDao = new SizeDao();
        
        this.productService = new ProductService(productDao, variantDao, sizeDao);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String idStr = request.getParameter("id");
        String redirectUrl = request.getContextPath() + "/user/product/searchResult.jsp"; 

        if (idStr != null && !idStr.trim().isEmpty()) {
            try {
                int id = Integer.parseInt(idStr);
                
                // 1. Lấy thông tin sản phẩm
                Products product = productService.getProductDetails(id); 

                if (product != null) {
                    // [QUAN TRỌNG 2] Lấy danh sách Biến thể và Tên Size
                    List<ProductVariants> variants = productService.getVariantsByProductId(id);
                    Map<Integer, String> sizeMap = productService.getSizeMap();

                    // 3. Gửi đầy đủ dữ liệu sang JSP
                    request.setAttribute("product", product);
                    request.setAttribute("variants", variants); // <-- Để hiện dropdown size
                    request.setAttribute("sizeMap", sizeMap);   // <-- Để hiện tên size (S, M, L)
                    
                    request.getRequestDispatcher("/user/product/info-products.jsp").forward(request, response);
                } else {
                    response.sendRedirect(redirectUrl);
                }
            } catch (NumberFormatException e) {
                response.sendRedirect(redirectUrl);
            }
        } else {
            response.sendRedirect(redirectUrl);
        }
    }
}