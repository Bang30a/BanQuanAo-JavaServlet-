package control.admin;

import dao.ProductDao;
import entity.Products;
import service.ProductService; // <-- Import service

import java.io.IOException;
import java.util.List; // <-- Import
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/admin/ProductsManager")
public class ProductsManagerServlet extends HttpServlet {

    private ProductService productService; // <-- Tham chiếu đến service

    @Override
    public void init() throws ServletException {
        // Khởi tạo DAO và "tiêm" vào Service
        ProductDao dao = new ProductDao();
        // Dùng chung ProductService với các servlet user
        this.productService = new ProductService(dao); 
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        // KHÔNG tạo DAO ở đây nữa

        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            action = "List";
        }

        try {
            switch (action) {
                case "SaveOrUpdate":
                    handleSaveOrUpdate(request, response);
                    break;
                case "List":
                    handleList(request, response);
                    break;
                case "AddOrEdit":
                    handleAddOrEdit(request, response);
                    break;
                case "Delete":
                    handleDelete(request, response);
                    break;
                default:
                    request.setAttribute("error", "Hành động không hợp lệ!");
                    request.getRequestDispatcher("/error.jsp").forward(request, response);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    // --- Tách các action ra thành các hàm riêng cho rõ ràng ---

    private void handleSaveOrUpdate(HttpServletRequest request, HttpServletResponse response)
            throws IOException, NumberFormatException {
        
        String idStr = request.getParameter("id");
        String name = request.getParameter("name");
        String desc = request.getParameter("description");
        String image = request.getParameter("image");
        double price = Double.parseDouble(request.getParameter("price"));

        int id = (idStr != null && !idStr.isEmpty()) ? Integer.parseInt(idStr) : 0;
        
        Products product = new Products(id, name, desc, price, image);
        
        // Gọi Service
        productService.saveOrUpdateProduct(product);

        // **SỬA LỖI:** Dùng Redirect (PRG Pattern)
        response.sendRedirect("ProductsManager?action=List");
    }

    private void handleList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Gọi service 1 LẦN (hàm mới của admin)
        List<Products> productList = productService.getAllProducts(); 
        
        request.setAttribute("list", productList);
        request.setAttribute("count", productList.size()); // Lấy size từ list đã có
        request.getRequestDispatcher("/admin/View-products.jsp").forward(request, response);
    }

    private void handleAddOrEdit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, NumberFormatException {
        
        String idParam = request.getParameter("id");
        int productId = (idParam != null && !idParam.isEmpty()) ? Integer.parseInt(idParam) : 0;
        
        // Service tự xử lý logic (nếu productId=0 trả về product rỗng)
        Products product = productService.getProductForEdit(productId); 

        request.setAttribute("PRODUCTS", product);
        request.setAttribute("ACTION", "SaveOrUpdate");
        request.getRequestDispatcher("/admin/ProductsManager.jsp").forward(request, response);
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException, NumberFormatException {
        
        String idParam = request.getParameter("id");
        if (idParam != null && !idParam.isEmpty()) {
            int productId = Integer.parseInt(idParam);
            productService.deleteProduct(productId); // Gọi Service
        }

        // **SỬA LỖI:** Dùng Redirect (PRG Pattern)
        response.sendRedirect("ProductsManager?action=List");
    }

    // --- Các hàm doGet, doPost, getServletInfo giữ nguyên ---

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