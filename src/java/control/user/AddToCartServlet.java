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

        // Lấy lại URL trang trước (ví dụ: product-detail.jsp?id=5)
        String referer = request.getHeader("Referer");
        if (referer == null || referer.isEmpty()) {
            referer = request.getContextPath() + "/user/view-products";
        }

        // --- KIỂM TRA ĐĂNG NHẬP ---
        if (user == null) {
            session.setAttribute("redirectAfterLogin", referer);
            session.setAttribute("loginError", "Vui lòng đăng nhập để thêm vào giỏ hàng!");
            response.sendRedirect(request.getContextPath() + "/user/Login.jsp");
            return;
        }

        try {
            int variantId = Integer.parseInt(request.getParameter("variantId"));
            int quantity = 1; // mặc định 1 nếu không có tham số
            try {
                quantity = Integer.parseInt(request.getParameter("quantity"));
            } catch (Exception ignore) {}

            List<CartBean> cart = (List<CartBean>) session.getAttribute("cart");
            ProductVariants variant = variantDao.findById(variantId);

            // Gọi CartService để thêm vào giỏ hàng
            cart = cartService.addToCart(cart, variant, quantity);
            session.setAttribute("cart", cart);

            // ✅ Thông báo thành công và quay lại trang sản phẩm
            session.setAttribute("addCartSuccess", "✅ Đã thêm sản phẩm vào giỏ hàng!");
            response.sendRedirect(referer);

        } catch (NumberFormatException e) {
            e.printStackTrace();
            session.setAttribute("addCartError", "❌ Mã sản phẩm không hợp lệ!");
            response.sendRedirect(referer);
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("addCartError", "❌ Thêm vào giỏ hàng thất bại!");
            response.sendRedirect(referer);
        }
    }
}
