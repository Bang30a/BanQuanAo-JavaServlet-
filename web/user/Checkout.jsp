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

    <!-- Bootstrap -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.4.0/css/bootstrap.min.css">
    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">

    <style>
        .checkout-box {
            background: #fff;
            padding: 30px 25px;
            border-radius: 12px;
            box-shadow: 0 4px 14px rgba(0,0,0,0.08);
        }

        h3 {
            font-weight: 600;
            color: #333;
        }

        table th {
            background-color: #f7f7f7 !important;
            text-transform: uppercase;
            font-size: 13px;
            letter-spacing: 0.5px;
        }

        table td, table th {
            vertical-align: middle !important;
        }

        label {
            font-weight: 500;
        }

        .btn-confirm {
            padding: 12px;
            font-size: 17px;
            font-weight: 600;
            border-radius: 6px;
        }

        .back-cart {
            display: inline-block;
            margin-top: 15px;
            font-size: 14px;
        }

    </style>
</head>

<body>
    <%@ include file="header.jsp" %>

    <div class="container mt-5 mb-5 checkout-box">
        <h3 class="text-center mb-4">Xác nhận đơn hàng</h3>

        <%
            List<CartBean> cart = (List<CartBean>) session.getAttribute("cart");
            double total = 0;
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

            if (cart == null || cart.isEmpty()) {
        %>
            <div class="alert alert-info text-center">
                <i class="fa fa-shopping-cart"></i> Giỏ hàng của bạn đang trống.
                <br>
                <a href="<%= request.getContextPath() %>/user/view-products"
                   class="btn btn-primary mt-3">← Tiếp tục mua sắm</a>
            </div>
        <%
            } else {
        %>

        <table class="table table-bordered">
            <thead class="thead-light">
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
                <% } %>
            </tbody>
            <tfoot>
                <tr>
                    <th colspan="3" class="text-right">Tổng cộng:</th>
                    <th style="color: #d9534f; font-weight: bold;"><%= formatter.format(total) %></th>
                </tr>
            </tfoot>
        </table>

        <form action="<%= request.getContextPath() %>/user/confirm-checkout" method="post">
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
                <input type="text" class="form-control" id="address" name="address"
                       placeholder="Nhập địa chỉ chi tiết..." required>
            </div>

            <div class="form-group">
                <label for="phone">Số điện thoại:</label>
                <input type="text" class="form-control" id="phone" name="phone"
                       placeholder="Nhập số điện thoại nhận hàng..." required>
            </div>

            <button type="submit" class="btn btn-success btn-block btn-confirm mt-4">
                <i class="fa fa-check-circle"></i> Xác nhận đặt hàng
            </button>
        </form>

        <a href="<%= request.getContextPath() %>/user/view-cart.jsp" class="back-cart">
            ← Quay lại giỏ hàng
        </a>

        <%
            }
        %>
    </div>

    <%@ include file="footer.jsp" %>

    <script src="https://code.jquery.com/jquery-3.5.1.slim.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@4.5.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
