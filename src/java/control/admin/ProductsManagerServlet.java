package control.admin;

import dao.ProductDao;
import entity.Products;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/admin/ProductsManager")
public class ProductsManagerServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        ProductDao dao = new ProductDao();
        Products product = null;

        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            action = "List"; // Mặc định chuyển về danh sách
        }

        try {
            switch (action) {
                case "SaveOrUpdate":
                    String idStr = request.getParameter("id");
                    String name = request.getParameter("name");
                    String desc = request.getParameter("description");
                    String image = request.getParameter("image");
                    double price = Double.parseDouble(request.getParameter("price"));

                    int id = (idStr != null && !idStr.isEmpty()) ? Integer.parseInt(idStr) : 0;
                    product = new Products(id, name, desc, price, image);

                    if (id == 0 || dao.findById(id) == null) {
                        dao.insert(product);
                    } else {
                        dao.update(product);
                    }

                    request.setAttribute("list", dao.getAllProducts());
                    request.setAttribute("count", dao.getAllProducts().size());
                    request.getRequestDispatcher("/admin/ProductsManager.jsp").forward(request, response);
                    break;

                case "List":
                    request.setAttribute("list", dao.getAllProducts());
                    request.setAttribute("count", dao.getAllProducts().size());
                    request.getRequestDispatcher("/admin/View-products.jsp").forward(request, response);
                    break;

                case "AddOrEdit":
                    String idParam = request.getParameter("id");
                    int productId = 0;
                    if (idParam != null && !idParam.isEmpty()) {
                        productId = Integer.parseInt(idParam);
                        product = dao.findById(productId);
                    }
                    if (product == null) {
                        product = new Products(); // Tạo mới nếu không tìm thấy
                    }

                    request.setAttribute("PRODUCTS", product);
                    request.setAttribute("ACTION", "SaveOrUpdate");
                    request.getRequestDispatcher("/admin/ProductsManager.jsp").forward(request, response);
                    break;

                case "Delete":
                    idParam = request.getParameter("id");
                    if (idParam != null && !idParam.isEmpty()) {
                        productId = Integer.parseInt(idParam);
                        dao.delete(productId);
                    }
                    request.setAttribute("list", dao.getAllProducts());
                    request.setAttribute("count", dao.getAllProducts().size());
                    request.getRequestDispatcher("/admin/View-products.jsp").forward(request, response);
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
