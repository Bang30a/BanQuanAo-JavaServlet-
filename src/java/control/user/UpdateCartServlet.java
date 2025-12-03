package control.user;

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

    @Override
    public void init() throws ServletException {
        this.cartService = new CartService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        List<CartBean> cart = (List<CartBean>) session.getAttribute("cart");
        
        // Trang giỏ hàng mặc định
        String cartPage = request.getContextPath() + "/user/order/view-cart.jsp";

        // Nếu giỏ null, quay về luôn
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

                // Gọi Service để xử lý logic update (hoặc xóa nếu qty=0)
                cartService.updateQuantity(cart, variantId, newQuantity);
                
                // Cập nhật lại session
                session.setAttribute("cart", cart);
            }
        } catch (NumberFormatException e) {
            System.err.println("UpdateCartServlet: Lỗi format số - " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Quay lại trang giỏ hàng
        response.sendRedirect(cartPage);
    }
}