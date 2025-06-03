package control.user;

import dao.OrderDao;
import dao.OrderDetailDao;
import entity.CartBean;
import entity.OrderDetails;
import entity.Orders;
import entity.ProductVariants;
import entity.Users;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import context.DBContext;

@WebServlet("/user/confirm-checkout")
public class ConfirmCheckoutServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        Users user = (Users) session.getAttribute("user");
        List<CartBean> cart = (List<CartBean>) session.getAttribute("cart");

        String address = request.getParameter("address");
        String phone = request.getParameter("phone");

        if (user == null) {
            response.sendRedirect("Login.jsp?error=notloggedin");
            return;
        }

        if (cart == null || cart.isEmpty()) {
            response.sendRedirect("view-cart.jsp?error=empty");
            return;
        }

        if (address == null || address.trim().isEmpty() || phone == null || phone.trim().isEmpty()) {
            response.sendRedirect("Checkout.jsp?error=missinginfo");
            return;
        }

        double total = 0;
        for (CartBean item : cart) {
            total += item.getTotalPrice();
        }

        Orders order = new Orders();
        order.setUserId(user.getId());
        order.setOrderDate(Timestamp.valueOf(LocalDateTime.now()));
        order.setTotal(total);
        order.setAddress(address.trim());
        order.setPhone(phone.trim());
        order.setStatus("Chờ xử lý");

        OrderDao orderDao = new OrderDao();
        OrderDetailDao detailDao = new OrderDetailDao();

        try (Connection conn = new DBContext().getConnection()) {
            conn.setAutoCommit(false); // Bắt đầu giao dịch

            int orderId = orderDao.addOrder(conn, order); // Gọi đúng phương thức nhận Connection
            if (orderId == -1) {
                conn.rollback();
                response.sendRedirect("Checkout.jsp?error=orderfail");
                return;
            }

            boolean success = true;
            for (CartBean item : cart) {
                ProductVariants variant = item.getProductVariant();

                OrderDetails detail = new OrderDetails();
                detail.setOrderId(orderId);
                detail.setProductVariantId(variant.getId());
                detail.setQuantity(item.getQuantity());
                detail.setPrice(variant.getPrice());

                if (!detailDao.addDetail(conn, detail)) {
                    success = false;
                    break;
                }
            }

            if (success) {
                conn.commit();
                session.removeAttribute("cart");
                response.sendRedirect("View-products.jsp?success=true");
            } else {
                conn.rollback();
                response.sendRedirect("Checkout.jsp?error=orderdetailfail");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("Checkout.jsp?error=exception");
        }
    }
}
