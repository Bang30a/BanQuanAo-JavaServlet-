package control.admin;

import dao.ProductVariantDao;
import entity.ProductVariants;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/admin/ProductVariantsManagerServlet")
public class ProductVariantsManagerServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        ProductVariantDao dao = new ProductVariantDao();
        ProductVariants variant = null;

        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            action = "List";
        }

        try {
            switch (action) {
                case "SaveOrUpdate":
                    int id = parseInt(request.getParameter("id"));
                    int productId = parseInt(request.getParameter("productId"));
                    int sizeId = parseInt(request.getParameter("sizeId"));
                    int stock = parseInt(request.getParameter("stock"));
                    double price = Double.parseDouble(request.getParameter("price"));

                    variant = new ProductVariants(id, productId, sizeId, stock, price, "", "");

                    if (id == 0 || dao.findById(id) == null) {
                        dao.insertVariant(variant);
                    } else {
                        dao.updateVariant(variant);
                    }

                    request.setAttribute("list", dao.getAllVariants());
                    request.getRequestDispatcher("/admin/ProductVariantsManager.jsp").forward(request, response);
                    break;

                case "AddOrEdit":
                    id = parseInt(request.getParameter("id"));
                    variant = dao.findById(id);
                    if (variant == null) variant = new ProductVariants();

                    request.setAttribute("VARIANT", variant);
                    request.setAttribute("ACTION", "SaveOrUpdate");
                    request.getRequestDispatcher("/admin/ProductVariantsManager.jsp").forward(request, response);
                    break;

                case "Delete":
                    id = parseInt(request.getParameter("id"));
                    dao.deleteVariant(id);

                    request.setAttribute("list", dao.getAllVariants());
                    request.getRequestDispatcher("/admin/View-variants.jsp").forward(request, response);
                    break;

                case "List":
                default:
                    request.setAttribute("list", dao.getAllVariants());
                    request.getRequestDispatcher("/admin/View-product-variants.jsp").forward(request, response);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    private int parseInt(String value) {
        return (value != null && !value.isEmpty()) ? Integer.parseInt(value) : 0;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
}
