<%@ page import="java.util.List" %>
<%@ page import="entity.CartBean" %>
<%@ page import="entity.ProductVariants" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%
    if (session.getAttribute("user") == null) {
        response.sendRedirect("Login.jsp");
        return;
    }
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Giỏ hàng</title>
    <link rel="stylesheet" type="text/css" href="style.css">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.0/css/bootstrap.min.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.0/js/bootstrap.min.js"></script>
</head>
<body>
<%@ include file="header.jsp" %>

<div class="container">
    <h2>Giỏ hàng</h2>
    <div class="mb-3">
        <a class="btn btn-primary btn-sm" href="${pageContext.request.contextPath}/user/View-products.jsp">Thêm sản phẩm</a>
        <a class="btn btn-success btn-sm" href="${pageContext.request.contextPath}/user/checkout">Thanh toán</a>
    </div>
    <table class="table table-bordered">
        <thead>
        <tr>
            <th>STT</th>
            <th>Mã Biến Thể</th>
            <th>Tên Sản Phẩm</th>
            <th>Giá</th>
            <th>Số lượng</th>
            <th>Thành tiền</th>
            <th>Thao tác</th>
        </tr>
        </thead>
        <tbody>
        <%
            List<CartBean> cart = (List<CartBean>) session.getAttribute("cart");
            if (cart != null && !cart.isEmpty()) {
                double total = 0;
                for (int i = 0; i < cart.size(); i++) {
                    CartBean item = cart.get(i);
                    ProductVariants variant = item.getProductVariant();
                    double price = variant.getPrice(); 
                    int quantity = item.getQuantity();
                    double subtotal = price * quantity;
                    total += subtotal;
        %>
        <tr>
            <td><%= i + 1 %></td>
            <td><%= variant.getId() %></td>
            <td><%= variant.getProductName() %></td>
            <td><%= price %></td>
            <td><%= quantity %></td>
            <td><%= subtotal %></td>
            <td>
                <a class="btn btn-danger btn-sm" href="${pageContext.request.contextPath}/user/remove-from-cart?index=<%= i %>">Xóa</a>
            </td>
        </tr>
        <%  } %>
        <tr>
            <td colspan="5" class="text-right"><strong>Tổng:</strong></td>
            <td colspan="2"><strong><%= total %></strong></td>
        </tr>
        <%
            } else {
        %>
        <tr>
            <td colspan="7" class="text-center">Giỏ hàng trống</td>
        </tr>
        <% } %>
        </tbody>
    </table>
</div>

<%@ include file="footer.jsp" %>
</body>
</html>
