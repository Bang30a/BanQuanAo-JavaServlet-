package control.user;

import dao.ProductDao;
import entity.Products;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/user/product-detail")
public class ProductDetailServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        ProductDao productDao = new ProductDao();
        Products product = productDao.findById(id);

        request.setAttribute("product", product);
        request.getRequestDispatcher("info-products.jsp").forward(request, response);
    }
}