package control.user;
import dao.ProductVariantDao;
import entity.CartBean;
import entity.ProductVariants;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/user/add-to-cart")
public class AddToCartServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int variantId = Integer.parseInt(request.getParameter("variantId"));
        HttpSession session = request.getSession();
        ProductVariantDao variantDao = new ProductVariantDao();
        ProductVariants variant = variantDao.findById(variantId);
        
        List<CartBean> cart = (List<CartBean>) session.getAttribute("cart");
        if (cart == null) cart = new ArrayList<>();
        
        boolean found = false;
        for (CartBean item : cart) {
            if (item.getProductVariant().getId() == variantId) {
                item.setQuantity(item.getQuantity() + 1);
                found = true;
                break;
            }
        }
        
        if (!found && variant != null) {
            cart.add(new CartBean(variant, 1));
        }
        
        session.setAttribute("cart", cart);
        
        // Sửa redirect về trang JSP thay vì servlet không tồn tại
        response.sendRedirect(request.getContextPath() + "/user/View-products.jsp");
    }
}