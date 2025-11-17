package control.user;

import dao.ProductDao;
import entity.Products;
import service.ProductService; // <-- Import service

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/user/product-detail")
public class ProductDetailServlet extends HttpServlet {
    
    private ProductService productService; // <-- Tham chiếu đến service

    /**
     * Khởi tạo service khi servlet bắt đầu
     */
    @Override
    public void init() throws ServletException {
        ProductDao productDao = new ProductDao();
        this.productService = new ProductService(productDao);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String idStr = request.getParameter("id");
        // Trang an toàn để chuyển hướng về nếu có lỗi
        String redirectUrl = request.getContextPath() + "/user/search-products"; 

        if (idStr != null) {
            try {
                int id = Integer.parseInt(idStr);
                
                // 1. Gọi Service để lấy sản phẩm (thay vì gọi DAO)
                Products product = productService.getProductDetails(id); 

                if (product != null) {
                    // 2. Tìm thấy -> Hiển thị trang chi tiết
                    request.setAttribute("product", product);
                    request.getRequestDispatcher("/user/info-products.jsp").forward(request, response);
                } else {
                    // 3. Không tìm thấy ID -> Về trang tìm kiếm
                    response.sendRedirect(redirectUrl);
                }
            } catch (NumberFormatException e) {
                // 4. ID không phải là số -> Về trang tìm kiếm
                response.sendRedirect(redirectUrl);
            }
        } else {
            // 5. Không có tham số ID -> Về trang tìm kiếm
            response.sendRedirect(redirectUrl);
        }
    }
}