<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="entity.CartBean" %>
<%@ page import="entity.ProductVariants" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>
<%@ page session="true" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Xác nhận đơn hàng</title>

    <!-- Bootstrap CSS -->
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
    <!-- Custom CSS -->
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/user/style.css">
</head>

<body class="bg-light">

    <%@ include file="header.jsp" %>

    <div class="container mt-5">
        <div class="card shadow p-4">
            <h3 class="mb-4 text-center">Xác nhận đơn hàng</h3>

            <%
                List<CartBean> cart = (List<CartBean>) session.getAttribute("cart");
                double total = 0;
                NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

                if (cart == null || cart.isEmpty()) {
            %>
                <div class="alert alert-info text-center">Giỏ hàng của bạn đang trống.</div>
            <%
                } else {
            %>

            <table class="table table-bordered table-hover bg-white">
                <thead class="thead-dark">
                    <tr>
                        <th>Sản phẩm</th>
                        <th>Giá</th>
                        <th>Số lượng</th>
                        <th>Thành tiền</th>
                    </tr>
                </thead>
                <tbody>
                <%
                    for (CartBean item : cart) {
                        ProductVariants pv = item.getProductVariant();
                        double itemTotal = pv.getPrice() * item.getQuantity();
                        total += itemTotal;
                %>
                    <tr>
                        <td><%= pv.getProductName() %></td>
                        <td><%= formatter.format(pv.getPrice()) %></td>
                        <td><%= item.getQuantity() %></td>
                        <td><%= formatter.format(itemTotal) %></td>
                    </tr>
                <%
                    }
                %>
                </tbody>
                <tfoot>
                    <tr>
                        <th colspan="3" class="text-right">Tổng cộng:</th>
                        <th><%= formatter.format(total) %></th>
                    </tr>
                </tfoot>
            </table>

            <!-- Form nhập địa chỉ và số điện thoại -->
            <form action="<%= request.getContextPath() %>/user/confirm-checkout" method="post">
                <input type="hidden" name="action" value="ConfirmCheckout">

                <%
                    String error = (String) request.getAttribute("error");
                    if (error != null) {
                %>
                    <div class="alert alert-danger"><%= error %></div>
                <%
                    }
                %>

                <div class="form-group">
                    <label for="address">Địa chỉ giao hàng:</label>
                    <input type="text" class="form-control" id="address" name="address" required>
                </div>

                <div class="form-group">
                    <label for="phoneNumber">Số điện thoại:</label>
                    <input type="text" class="form-control" id="phone" name="phone" required>
                </div>

                <button type="submit" class="btn btn-success btn-block">Xác nhận đặt hàng</button>
            </form>

            <%
                }
            %>
        </div>
    </div>

    <%@ include file="footer.jsp" %>

    <!-- Optional Bootstrap JS -->
    <script src="https://code.jquery.com/jquery-3.5.1.slim.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@4.5.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
