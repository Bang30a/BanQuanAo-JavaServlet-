package control.user;

import dao.ProductDao;
import entity.Products;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/user/search-products")
public class SearchProductServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            handleRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(SearchProductServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            handleRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(SearchProductServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, Exception {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        ProductDao productDao = new ProductDao();

        if ("detail".equalsIgnoreCase(action)) {
            String idStr = request.getParameter("id");
            if (idStr != null) {
                try {
                    int id = Integer.parseInt(idStr);
                    Products product = productDao.findById(id);
                    if (product != null) {
                        request.setAttribute("product", product);
                        request.getRequestDispatcher("/user/info-products.jsp").forward(request, response);
                    } else {
                        response.sendRedirect("search-products");
                    }
                } catch (NumberFormatException e) {
                    response.sendRedirect("search-products");
                }
            } else {
                response.sendRedirect("search-products");
            }
        } else {
            String keyword = request.getParameter("keyword");
            List<Products> results;

            if (keyword != null && !keyword.trim().isEmpty()) {
                results = productDao.searchByKeyword(keyword.trim());
            } else {
                results = productDao.getAllProducts();
            }

            request.setAttribute("productList", results);
            request.setAttribute("keyword", keyword);
            request.getRequestDispatcher("searchResults.jsp").forward(request, response);
        }
    }
}
