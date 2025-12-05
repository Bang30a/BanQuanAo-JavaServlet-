package control.user;

import dao.ProductVariantDao;
import entity.CartBean;
import entity.ProductVariants;
import entity.Users;
import service.CartService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/user/add-to-cart")
public class AddToCartServlet extends HttpServlet {

    private CartService cartService;
    private ProductVariantDao variantDao;

    @Override
    public void init() throws ServletException {
        this.cartService = new CartService();
        this.variantDao = new ProductVariantDao();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        Users user = (Users) session.getAttribute("user");
        String referer = request.getHeader("Referer");
        
        // [SỬA LẠI] Fallback về trang chủ Servlet
        if (referer == null || referer.isEmpty()) {
            referer = request.getContextPath() + "/user/view-products"; 
        }

        if (user == null) {
            session.setAttribute("redirectAfterLogin", referer);
            session.setAttribute("loginError", "Vui lòng đăng nhập để thêm vào giỏ hàng!");
            response.sendRedirect(request.getContextPath() + "/user/auth/Login.jsp");
            return;
        }

        try {
            int variantId = Integer.parseInt(request.getParameter("variantId"));
            int quantity = 1; 
            try { quantity = Integer.parseInt(request.getParameter("quantity")); } catch (Exception e) {}

            List<CartBean> cart = (List<CartBean>) session.getAttribute("cart");
            ProductVariants variant = variantDao.findById(variantId);

            if (variant != null) {
                cart = cartService.addToCart(cart, variant, quantity);
                session.setAttribute("cart", cart);
                session.setAttribute("addCartSuccess", "Đã thêm [" + variant.getProductName() + "] vào giỏ hàng!");
            } else {
                session.setAttribute("addCartError", "Sản phẩm không tồn tại!");
            }
            response.sendRedirect(referer);

        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("addCartError", "Lỗi khi thêm giỏ hàng.");
            response.sendRedirect(referer);
        }
    }
}