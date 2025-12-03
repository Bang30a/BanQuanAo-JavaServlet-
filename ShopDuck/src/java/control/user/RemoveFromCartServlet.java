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

/**
 * Servlet này xử lý việc xóa một sản phẩm khỏi giỏ hàng.
 * Nó lắng nghe tại /user/remove-from-cart và hỗ trợ phương thức GET.
 */
@WebServlet("/user/remove-from-cart")
public class RemoveFromCartServlet extends HttpServlet {

    private CartService cartService;

    @Override
    public void init() throws ServletException {
        // Khởi tạo CartService
        this.cartService = new CartService();
    }

    /**
     * Xử lý yêu cầu GET để xóa sản phẩm.
     * Chúng ta dùng doGet vì liên kết xóa thường là thẻ <a>, vốn gửi yêu cầu GET.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        
        // Lấy giỏ hàng từ session
        List<CartBean> cart = (List<CartBean>) session.getAttribute("cart");
        
        // [SỬA PATH] URL để chuyển hướng về sau khi xóa (trang giỏ hàng mới)
        String redirectUrl = request.getContextPath() + "/user/order/view-cart.jsp";

        // Nếu giỏ hàng chưa tồn tại, không cần làm gì, quay về luôn
        if (cart == null) {
            response.sendRedirect(redirectUrl);
            return;
        }

        try {
            // 1. Lấy vị trí (index) của sản phẩm cần xóa từ URL
            String indexStr = request.getParameter("index");
            
            if (indexStr != null) {
                int index = Integer.parseInt(indexStr);
                
                // 2. Gọi CartService để xóa
                // Service sẽ xử lý logic kiểm tra index hợp lệ
                cartService.removeFromCart(cart, index);

                // 3. Cập nhật lại giỏ hàng trong session
                session.setAttribute("cart", cart);
            }

        } catch (NumberFormatException e) {
            // Lỗi nếu 'index' không phải là số
            System.err.println("RemoveFromCartServlet: Tham số index không hợp lệ.");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("RemoveFromCartServlet: Lỗi không xác định.");
            e.printStackTrace();
        }

        // 4. Chuyển hướng người dùng trở lại trang giỏ hàng
        response.sendRedirect(redirectUrl);
    }
}