package control.user;

import entity.CartBean;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/user/remove-from-cart")
public class RemoveFromCartServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int removeIndex = Integer.parseInt(request.getParameter("index"));
        HttpSession session = request.getSession();
        List<CartBean> cart = (List<CartBean>) session.getAttribute("cart");

        if (cart != null && removeIndex >= 0 && removeIndex < cart.size()) {
            cart.remove(removeIndex);
        }

        response.sendRedirect("/user/view-cart.jsp");
    }
}