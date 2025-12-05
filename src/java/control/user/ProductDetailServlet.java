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

@WebServlet("/user/product-detail")
public class ProductDetailServlet extends HttpServlet {
    
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
        
        String idStr = request.getParameter("id");
        String redirectUrl = request.getContextPath() + "/user/product/searchResult.jsp"; 

        try {
            // 1. Kiểm tra ID hợp lệ (Không null, không rỗng, không toàn khoảng trắng)
            if (idStr != null && !idStr.trim().isEmpty()) {
                
                int id = Integer.parseInt(idStr.trim()); // Trim trước khi parse
                
                // 2. Lấy thông tin sản phẩm
                Products product = productService.getProductDetails(id); 

                if (product != null) {
                    // 3. Lấy biến thể và size
                    List<ProductVariants> variants = productService.getVariantsByProductId(id);
                    Map<Integer, String> sizeMap = productService.getSizeMap();

                    request.setAttribute("product", product);
                    request.setAttribute("variants", variants);
                    request.setAttribute("sizeMap", sizeMap);
                    
                    request.getRequestDispatcher("/user/product/info-products.jsp").forward(request, response);
                } else {
                    // Không tìm thấy SP -> Quay về trang tìm kiếm
                    response.sendRedirect(redirectUrl);
                }

            } else {
                // ID rỗng hoặc null -> Quay về trang tìm kiếm
                response.sendRedirect(redirectUrl);
            }

        } catch (NumberFormatException e) {
            // ID không phải số
            response.sendRedirect(redirectUrl);
        } catch (Exception e) {
            // [QUAN TRỌNG] Bắt lỗi hệ thống (DB lỗi, NullPointer...) để không sập trang
            Logger.getLogger(ProductDetailServlet.class.getName()).log(Level.SEVERE, "System Error in ProductDetail", e);
            response.sendRedirect(redirectUrl);
        }
    }
}