package control.user;

import dao.ProductDao;
import entity.Products;
import service.ProductService; // Dùng Service chúng ta đã tạo

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/user/view-products") // Đây là servlet trang chủ
public class ViewProductServlet extends HttpServlet {
    
    private ProductService productService; // Dùng Service

    @Override
    public void init() throws ServletException {
        // Khởi tạo service (giống các servlet khác)
        // Dùng chung ProductService đã viết
        this.productService = new ProductService(new ProductDao());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // --- 1. SERVLET LẤY DỮ LIỆU ---
        // Service sẽ tự động xử lý lỗi (try-catch)
        
        // Lấy 8 sản phẩm mới nhất
        List<Products> allProducts = productService.getAllProducts();
        List<Products> productList = allProducts.subList(0, Math.min(allProducts.size(), 8));

        // Lấy 6 sản phẩm "áo"
        List<Products> allShirts = productService.searchProducts("áo");
        List<Products> shirtList = allShirts.subList(0, Math.min(allShirts.size(), 6));

        // Lấy 6 sản phẩm "quần"
        List<Products> allPants = productService.searchProducts("quần");
        List<Products> pantsList = allPants.subList(0, Math.min(allPants.size(), 6));

        // --- 2. GỬI DỮ LIỆU SANG JSP ---
        request.setAttribute("productList", productList);
        request.setAttribute("shirtList", shirtList);
        request.setAttribute("pantsList", pantsList);

        // --- 3. FORWARD TỚI TRANG JSP (JSP CHỈ VIỆC HIỂN THỊ) ---
        request.getRequestDispatcher("/user/View-products.jsp").forward(request, response);
    }
}