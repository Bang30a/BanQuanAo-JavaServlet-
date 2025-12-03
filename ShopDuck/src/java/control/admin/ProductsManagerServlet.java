package control.admin;

import dao.ProductDao;
import entity.Products;
import service.ProductService;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/ProductsManager")
public class ProductsManagerServlet extends HttpServlet {

    private ProductService productService;

    @Override
    public void init() throws ServletException {
        // Khởi tạo DAO và Service
        ProductDao dao = new ProductDao();
        this.productService = new ProductService(dao); 
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            action = "List";
        }

        try {
            switch (action) {
                // --- NHÓM CUD (Create, Update, Delete) ---
                case "SaveOrUpdate":
                    handleSaveOrUpdate(request, response);
                    break;
                case "Delete":
                    handleDelete(request, response);
                    break;
                
                // --- NHÓM READ (Hiển thị Form, Danh sách) ---
                case "AddOrEdit":
                    handleAddOrEdit(request, response);
                    break;
                case "List":
                default:
                    handleList(request, response);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            // Nếu lỗi, quay về danh sách
            handleList(request, response);
        }
    }

    // 1. XỬ LÝ LƯU
    private void handleSaveOrUpdate(HttpServletRequest request, HttpServletResponse response)
            throws IOException, NumberFormatException {
        
        String idStr = request.getParameter("id");
        String name = request.getParameter("name");
        String desc = request.getParameter("description");
        String image = request.getParameter("image");
        double price = 0;
        try {
             price = Double.parseDouble(request.getParameter("price"));
        } catch (NumberFormatException e) {}

        int id = 0;
        try {
            if (idStr != null && !idStr.isEmpty()) id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {}
        
        Products product = new Products(id, name, desc, price, image);
        
        // Gọi Service để lưu
        productService.saveOrUpdateProduct(product);

        // [REDIRECT] Về trang danh sách
        response.sendRedirect("ProductsManager?action=List");
    }

    // 2. XỬ LÝ XÓA
    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException, NumberFormatException {
        
        String idParam = request.getParameter("id");
        try {
            if (idParam != null && !idParam.isEmpty()) {
                int productId = Integer.parseInt(idParam);
                productService.deleteProduct(productId);
            }
        } catch (NumberFormatException e) { e.printStackTrace(); }

        // [REDIRECT] Về trang danh sách
        response.sendRedirect("ProductsManager?action=List");
    }

    // 3. HIỂN THỊ FORM NHẬP LIỆU
    private void handleAddOrEdit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, NumberFormatException {
        
        String idParam = request.getParameter("id");
        int productId = 0;
        try {
            if (idParam != null && !idParam.isEmpty()) productId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {}
        
        // Service xử lý: nếu productId=0 -> trả về new Product()
        Products product = productService.getProductForEdit(productId); 

        request.setAttribute("PRODUCTS", product); // Tên biến khớp với JSP ProductsManager.jsp
        request.setAttribute("ACTION", "SaveOrUpdate");
        
        // [FORWARD] Trỏ về file JSP trong thư mục admin/products/
        request.getRequestDispatcher("/admin/products/ProductsManager.jsp").forward(request, response);
    }

    // 4. HIỂN THỊ DANH SÁCH
    private void handleList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        List<Products> productList = productService.getAllProducts(); 
        
        // [SỬA TÊN BIẾN] Đặt là "PRODUCTS" để khớp với View-products.jsp
        request.setAttribute("PRODUCTS", productList); 
        
        // [FORWARD] Trỏ về file JSP trong thư mục admin/products/
        request.getRequestDispatcher("/admin/products/View-products.jsp").forward(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Product management servlet";
    }
}