package control.user;

import dao.ProductVariantDao;
import entity.ProductVariants;
import entity.CartBean;
import service.CartService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/user/update-cart")
public class UpdateCartServlet extends HttpServlet {

    private CartService cartService;
    private ProductVariantDao variantDao; // [MỚI] Biến instance cho DAO

    // [MỚI] Setter để Inject Mock DAO khi test
    public void setVariantDao(ProductVariantDao variantDao) {
        this.variantDao = variantDao;
    }

    // [MỚI] Getter lazy load (để chạy thật vẫn ổn)
    private ProductVariantDao getVariantDao() {
        if (variantDao == null) {
            variantDao = new ProductVariantDao();
        }
        return variantDao;
    }

    @Override
    public void init() throws ServletException {
        this.cartService = new CartService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        List<CartBean> cart = (List<CartBean>) session.getAttribute("cart");
        
        String cartPage = request.getContextPath() + "/user/order/view-cart.jsp";

        if (cart == null) {
            response.sendRedirect(cartPage);
            return;
        }

        try {
            String idStr = request.getParameter("variantId");
            String qtyStr = request.getParameter("quantity");

            if (idStr != null && qtyStr != null) {
                int variantId = Integer.parseInt(idStr);
                int newQuantity = Integer.parseInt(qtyStr);

                // --- LOGIC KIỂM TRA TỒN KHO ---
                // [SỬA] Dùng getter thay vì new trực tiếp
                ProductVariantDao dao = getVariantDao(); 
                ProductVariants variant = dao.findById(variantId);

                if (variant != null) {
                    int currentStock = variant.getStock();

                    if (newQuantity > currentStock) {
                        newQuantity = currentStock; // Reset về max
                        session.setAttribute("cartError", "Rất tiếc! Sản phẩm này chỉ còn " + currentStock + " cái trong kho.");
                    }
                }

                // Gọi Service
                cartService.updateQuantity(cart, variantId, newQuantity);
                session.setAttribute("cart", cart);
            }
        } catch (NumberFormatException e) {
            System.err.println("UpdateCartServlet: Lỗi format số - " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        response.sendRedirect(cartPage);
    }
}