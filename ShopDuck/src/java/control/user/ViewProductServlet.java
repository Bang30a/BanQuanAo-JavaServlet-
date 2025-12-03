package control.user;

import dao.ProductDao;
import dao.ProductVariantDao;
import dao.SizeDao;
import entity.Products;
import service.ProductService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

// Servlet này đóng vai trò là Trang Chủ
@WebServlet(name = "HomeServlet", urlPatterns = {"/home", "/user/view-products"})
public class ViewProductServlet extends HttpServlet {
    
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
        
        request.setCharacterEncoding("UTF-8");

        try {
            // 1. Lấy dữ liệu sản phẩm
            List<Products> allProducts = productService.getAllProducts();
            List<Products> productList = allProducts.size() > 8 ? allProducts.subList(0, 8) : allProducts;

            List<Products> allShirts = productService.searchProducts("áo");
            List<Products> shirtList = allShirts.size() > 4 ? allShirts.subList(0, 4) : allShirts;

            List<Products> allPants = productService.searchProducts("quần");
            List<Products> pantsList = allPants.size() > 4 ? allPants.subList(0, 4) : allPants;

            // 2. Gửi dữ liệu sang JSP
            request.setAttribute("productList", productList);
            request.setAttribute("shirtList", shirtList);
            request.setAttribute("pantsList", pantsList);

            // [SỬA LẠI] Forward về đúng file JSP trong thư mục user/product/
            request.getRequestDispatcher("/user/product/View-products.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            // Nếu lỗi, vẫn forward về trang này (có thể hiện trống hoặc lỗi)
            request.getRequestDispatcher("/user/product/View-products.jsp").forward(request, response);
        }
    }
}